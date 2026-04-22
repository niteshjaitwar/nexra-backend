package com.nexra.hrms.nexra.common.ratelimit;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Enables the RateLimitProperties binding so the GlobalRateLimitFilter can
 * consume environment driven configuration. Kept as its own class to avoid
 * bean definition order pitfalls when the filter itself is scanned.
 *
 * @author niteshjaitwar
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {
}
