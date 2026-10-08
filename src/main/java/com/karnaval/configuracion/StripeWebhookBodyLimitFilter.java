package com.karnaval.configuracion;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ReadListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class StripeWebhookBodyLimitFilter extends OncePerRequestFilter {
    private static final int MAX_BODY_BYTES = 65_536;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        if (!"POST".equals(request.getMethod())
                || !request.getRequestURI().equals(request.getContextPath() + "/stripe/webhook")) {
            chain.doFilter(request, response);
            return;
        }
        if (request.getContentLengthLong() > MAX_BODY_BYTES) {
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            return;
        }
        byte[] body = request.getInputStream().readNBytes(MAX_BODY_BYTES + 1);
        if (body.length > MAX_BODY_BYTES) {
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            return;
        }
        chain.doFilter(new BoundedBodyRequest(request, body), response);
    }

    private static final class BoundedBodyRequest extends HttpServletRequestWrapper {
        private final byte[] body;

        private BoundedBodyRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public int getContentLength() { return body.length; }

        @Override
        public long getContentLengthLong() { return body.length; }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream stream = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override public int read() { return stream.read(); }
                @Override public boolean isFinished() { return stream.available() == 0; }
                @Override public boolean isReady() { return true; }
                @Override public void setReadListener(ReadListener listener) {
                    throw new UnsupportedOperationException("Asynchronous webhook reading is not supported");
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
