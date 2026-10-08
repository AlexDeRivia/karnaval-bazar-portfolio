package com.karnaval.configuracion;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class PublicRequestRateLimitFilter extends OncePerRequestFilter {
    private static final long WINDOW_MILLIS = 15 * 60 * 1000L;
    private static final int CHECKOUT_LIMIT = 20;
    private static final int LOGIN_LIMIT = 30;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (!"POST".equals(request.getMethod()) || (!"/checkout".equals(path) && !"/login".equals(path))) {
            chain.doFilter(request, response);
            return;
        }
        int limit = "/checkout".equals(path) ? CHECKOUT_LIMIT : LOGIN_LIMIT;
        long window = System.currentTimeMillis() / WINDOW_MILLIS;
        // Remote address comes from the servlet container, not an untrusted X-Forwarded-For header.
        String key = path + ':' + request.getRemoteAddr();
        if (windows.size() >= 10_000) {
            windows.entrySet().removeIf(entry -> entry.getValue().bucket < window);
            if (windows.size() >= 10_000 && !windows.containsKey(key)) {
                response.sendError(429);
                return;
            }
        }
        Window current = windows.compute(key, (ignored, previous) -> previous == null || previous.bucket != window
                ? new Window(window, 1) : new Window(window, previous.count + 1));
        if (current.count > limit) {
            response.setHeader("Retry-After", String.valueOf((window + 1) * 900 - System.currentTimeMillis() / 1000));
            response.sendError(429);
            return;
        }
        chain.doFilter(request, response);
    }

    private record Window(long bucket, int count) {}
}
