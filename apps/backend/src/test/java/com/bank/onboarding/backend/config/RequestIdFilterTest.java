package com.bank.onboarding.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @Test
    void doFilterInternal_setsRequestIdHeaderAndMDC() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        doAnswer(invocation -> {
            // Inside the filter chain, MDC should have requestId
            assertNotNull(MDC.get("requestId"));
            assertEquals(8, MDC.get("requestId").length());
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);

        // After filter, MDC should be cleared
        assertNull(MDC.get("requestId"));

        // Response should have the header
        String headerValue = response.getHeader("X-Request-Id");
        assertNotNull(headerValue);
        assertEquals(8, headerValue.length());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_clearsMDC_evenOnException() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        doThrow(new ServletException("test error")).when(filterChain).doFilter(request, response);

        assertThrows(ServletException.class, () -> filter.doFilterInternal(request, response, filterChain));

        // MDC should still be cleared after exception
        assertNull(MDC.get("requestId"));
    }
}
