package com.nexra.hrms.nexra.common.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nexra.hrms.nexra.common.api.ApiResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Platform wide token bucket rate limiter backed by bucket4j. Buckets are
 * keyed on the authenticated principal when available, falling back to the
 * client IP. Runs after the correlation filter but before Spring Security so
 * abusive traffic is shed before any authentication work is performed.
 * Returns a canonical ApiResponse envelope on throttle with an accurate
 * Retry-After header.
 *
 * @author niteshjaitwar
 */
@Slf4j
@Component("nexraGlobalRateLimitFilter")
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class GlobalRateLimitFilter extends OncePerRequestFilter {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private final RateLimitProperties properties;
    private final ObjectProvider<ObjectMapper> objectMapperProvider;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(30))
            .maximumSize(100_000)
            .build();
    private volatile ObjectMapper cachedObjectMapper;

    /**
     * Builds the filter with injected configuration and a lazy Jackson
     * mapper provider. The provider is lazy so the filter can instantiate
     * even before Jackson auto configuration has wired its primary mapper,
     * which is important because this filter is a high precedence servlet
     * filter.
     *
     * @param properties           externalised rate limit settings.
     * @param objectMapperProvider lazy Jackson mapper provider.
     */
    public GlobalRateLimitFilter(final RateLimitProperties properties, final ObjectProvider<ObjectMapper> objectMapperProvider) {
        this.properties = properties;
        this.objectMapperProvider = objectMapperProvider;
    }

    /**
     * Lazily resolves the Jackson ObjectMapper, falling back to a locally
     * constructed instance when the application context does not expose
     * one. The result is cached to avoid repeated lookups.
     *
     * @return non null ObjectMapper instance.
     */
    private ObjectMapper resolveObjectMapper() {
        ObjectMapper mapper = cachedObjectMapper;
        if (mapper == null) {
            mapper = objectMapperProvider.getIfAvailable();
            if (mapper == null) {
                mapper = new ObjectMapper();
                mapper.findAndRegisterModules();
            }
            cachedObjectMapper = mapper;
        }
        return mapper;
    }

    /**
     * Short circuits the filter for excluded paths or when rate limiting is
     * disabled.
     *
     * @param request incoming HTTP request.
     * @return true when the request MUST NOT be filtered.
     */
    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        if (!properties.enabled()) {
            return true;
        }
        final String path = request.getRequestURI();
        if (!StringUtils.hasText(path)) {
            return false;
        }
        for (final String pattern : properties.excludedPaths()) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Consumes one token from the caller's bucket and either forwards the
     * request or emits HTTP 429 with a canonical envelope.
     *
     * @param request     incoming HTTP request.
     * @param response    outbound HTTP response.
     * @param filterChain downstream filter chain.
     * @throws ServletException when a downstream filter fails.
     * @throws IOException      when response writing fails.
     */
    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain) throws ServletException, IOException {
        final String key = resolveBucketKey(request);
        final Bucket bucket = buckets.asMap().computeIfAbsent(key, ignored -> newBucket());
        final ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1L);
        if (probe.isConsumed()) {
            response.setHeader("X-RateLimit-Limit", String.valueOf(properties.capacity()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
            return;
        }
        final long retryAfterSeconds = Math.max(1L, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
        log.warn("GlobalRateLimitFilter - doFilterInternal() - throttle key={}, retryAfterSeconds={}", key, retryAfterSeconds);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setHeader("X-RateLimit-Limit", String.valueOf(properties.capacity()));
        response.setHeader("X-RateLimit-Remaining", "0");
        final ApiResponse<Void> envelope = ApiResponse.failure(
                "RATE_LIMITED",
                "Too many requests. Please retry after " + retryAfterSeconds + " seconds.");
        resolveObjectMapper().writeValue(response.getWriter(), envelope);
    }

    /**
     * Picks the authenticated principal when present, otherwise uses the
     * request source IP so anonymous abuse is also scoped.
     *
     * @param request incoming HTTP request.
     * @return non blank bucket key.
     */
    private String resolveBucketKey(final HttpServletRequest request) {
        final String remoteUser = request.getRemoteUser();
        if (StringUtils.hasText(remoteUser)) {
            return "user:" + remoteUser;
        }
        final String forwarded = request.getHeader(X_FORWARDED_FOR);
        final String ip;
        if (StringUtils.hasText(forwarded)) {
            final int comma = forwarded.indexOf(',');
            ip = comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
        } else {
            ip = request.getRemoteAddr();
        }
        return "ip:" + (StringUtils.hasText(ip) ? ip : "unknown");
    }

    /**
     * Lazily creates a new greedy refill bucket with the configured capacity
     * and period.
     *
     * @return fresh bucket instance.
     */
    private Bucket newBucket() {
        final Bandwidth bandwidth = Bandwidth.builder()
                .capacity(properties.capacity())
                .refillGreedy(properties.capacity(), resolvePeriod())
                .build();
        return Bucket.builder().addLimit(bandwidth).build();
    }

    /**
     * Resolves the configured refill period with a minute floor to protect
     * against misconfiguration.
     *
     * @return safe non zero Duration.
     */
    private Duration resolvePeriod() {
        return properties.refillPeriod().isZero() || properties.refillPeriod().isNegative()
                ? Duration.ofMinutes(1)
                : properties.refillPeriod();
    }
}
