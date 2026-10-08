package com.karnaval;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.karnaval.configuracion.PublicRequestRateLimitFilter;

class PublicRequestRateLimitFilterTests {
    @Test
    void limitsCheckoutRequestsFromTheSameAddress() throws Exception {
        PublicRequestRateLimitFilter filter = new PublicRequestRateLimitFilter();
        for (int attempt = 1; attempt <= 21; attempt++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/checkout");
            request.setRemoteAddr("192.0.2.10");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, new MockFilterChain());
            assertThat(response.getStatus()).isEqualTo(attempt <= 20 ? 200 : 429);
            if (attempt == 21) {
                assertThat(response.getHeader("Retry-After")).isNotBlank();
            }
        }
    }
}
