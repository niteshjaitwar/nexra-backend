package com.nexra.hrms.nexra.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.UUID;

/**
 * Platform wide correlation id filter. Registered at HIGHEST_PRECEDENCE so
 * every request entering the monolith is tagged with a stable request id
 * before any module scoped filter runs. If a module filter later sees an
 * existing MDC key it MUST reuse the value, never overwrite it. The header
 * value is echoed back to the client so distributed tracing can stitch the
 * full request path together.
 *
 * @author niteshjaitwar
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component("nexraCorrelationIdFilter")
public class CorrelationIdFilter extends OncePerRequestFilter {
    private static final Pattern SAFE_REQUEST_ID_PATTERN = Pattern.compile("^[A-Za-z0-9._:-]{1,64}$");

    /**
     * MDC key used in every logback pattern across the platform.
     */
    public static final String MDC_KEY = "requestId";

    /**
     * HTTP header name for inbound correlation and outbound echo.
     */
    public static final String HEADER_NAME = "X-Request-Id";

    /**
     * Resolves (or mints) a correlation id, stores it in the MDC for the
     * lifetime of the request and echoes it back to the client.
     *
     * @param request     incoming HTTP request.
     * @param response    outbound HTTP response.
     * @param filterChain downstream filter chain.
     * @throws ServletException when a downstream filter fails.
     * @throws IOException      when a downstream filter fails with IO.
     */
    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain) throws ServletException, IOException {
        final String existing = MDC.get(MDC_KEY);
        final boolean alreadySet = StringUtils.hasText(existing);
        final String requestId = alreadySet ? existing : resolveRequestId(request);
        if (!alreadySet) {
            MDC.put(MDC_KEY, requestId);
        }
        response.setHeader(HEADER_NAME, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (!alreadySet) {
                MDC.remove(MDC_KEY);
            }
        }
    }

    /**
     * Extracts the incoming correlation id from the request header or mints
     * a fresh UUID when the header is missing.
     *
     * @param request incoming HTTP request.
     * @return non blank request id suitable for MDC propagation.
     */
    private String resolveRequestId(final HttpServletRequest request) {
        final String value = request.getHeader(HEADER_NAME);
        if (StringUtils.hasText(value)) {
            final String candidate = value.trim();
            if (SAFE_REQUEST_ID_PATTERN.matcher(candidate).matches()) {
                return candidate;
            }
        }
        return UUID.randomUUID().toString();
    }
}
