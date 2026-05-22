package com.nexra.hrms.nexra.modules.hrms.leave.security;

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
 * Propagates a stable request identifier through leave logs and responses.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Component
public class LeaveRequestCorrelationFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String MDC_KEY = "requestId";
    private static final String MODULE_PATH_PREFIX = "/api/v1/leave/";

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        return !request.getRequestURI().startsWith(MODULE_PATH_PREFIX);
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
        String value = request.getHeader(REQUEST_ID_HEADER);
        return StringUtils.hasText(value) ? value.trim() : UUID.randomUUID().toString();
    }
}
