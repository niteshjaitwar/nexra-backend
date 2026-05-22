package com.nexra.hrms.nexra.modules.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Ensures every request carries a stable correlation identifier for tracing and audit logs.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Component
public class RequestCorrelationFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String MDC_KEY = "requestId";
    private static final String[] MODULE_PATH_PREFIXES = {
        "/api/v1/auth/",
        "/api/v1/tenants",
        "/api/v1/platform/",
        "/api/v1/admin/",
        "/api/v1/oauth-clients"
    };

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        final String uri = request.getRequestURI();
        for (String prefix : MODULE_PATH_PREFIXES) {
            if (uri.startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void doFilterInternal(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final FilterChain filterChain
    ) throws ServletException, IOException {
        final String existing = MDC.get(MDC_KEY);
        final boolean alreadySet = StringUtils.hasText(existing);
        final String requestId = alreadySet ? existing : resolveRequestId(request);
        if (!alreadySet) {
            MDC.put(MDC_KEY, requestId);
        }
        response.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (!alreadySet) {
                MDC.remove(MDC_KEY);
            }
        }
    }

    private String resolveRequestId(final HttpServletRequest request) {
        String existing = request.getHeader(REQUEST_ID_HEADER);
        return StringUtils.hasText(existing) ? existing.trim() : UUID.randomUUID().toString();
    }
}
