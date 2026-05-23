package com.nexra.hrms.nexra.common.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * Externalised configuration for the platform wide rate limiter. All values
 * can be overridden per environment under the prefix
 * {@code nexra.common.rate-limit}. Defaults are tuned for a balanced
 * protection against abuse without impeding typical SaaS usage.
 *
 * @param enabled         master switch used to disable rate limiting in tests.
 * @param capacity        maximum number of requests allowed in the burst window.
 * @param refillPeriod    duration over which the bucket refills to capacity.
 * @param distributedEnabled enables Redis-backed shared limiting for multi-instance deployments.
 * @param trustForwardedHeaders whether X-Forwarded-For should be trusted for client IP resolution.
 * @param redisKeyPrefix  namespace prefix for Redis rate-limit keys.
 * @param excludedPaths   ant style path patterns excluded from rate limiting.
 * @author niteshjaitwar
 */
@ConfigurationProperties(prefix = "nexra.common.rate-limit")
public record RateLimitProperties(
        boolean enabled,
        long capacity,
        Duration refillPeriod,
        boolean distributedEnabled,
        boolean trustForwardedHeaders,
        String redisKeyPrefix,
        List<String> excludedPaths) {

    /**
     * Applies defaults for optional properties when none are supplied by the
     * environment so the filter always has a safe configuration.
     *
     * @param enabled       master switch, defaulting to true when null.
     * @param capacity      bucket capacity, defaulting to 120 when null.
     * @param refillPeriod  refill duration, defaulting to one minute when null.
     * @param distributedEnabled distributed mode flag, defaulting to false when null.
     * @param trustForwardedHeaders whether forwarded headers are trusted, defaulting to false.
     * @param redisKeyPrefix redis key prefix, defaulting to {@code nexra:ratelimit}.
     * @param excludedPaths excluded path patterns, defaulting to actuator and OpenAPI endpoints.
     */
    public RateLimitProperties {
        if (capacity <= 0L) {
            capacity = 120L;
        }
        if (refillPeriod == null || refillPeriod.isZero() || refillPeriod.isNegative()) {
            refillPeriod = Duration.ofMinutes(1);
        }
        if (redisKeyPrefix == null || redisKeyPrefix.isBlank()) {
            redisKeyPrefix = "nexra:ratelimit";
        }
        if (excludedPaths == null || excludedPaths.isEmpty()) {
            excludedPaths = List.of(
                    "/actuator/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/favicon.ico");
        }
    }
}
