package com.nexra.hrms.nexra.common.web;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Reusable security headers policy applied by every SecurityFilterChain in
 * the platform. Enforces HSTS, a strict Content Security Policy for APIs,
 * Referrer Policy, X-Content-Type-Options and frame options. Modules call
 * {@link #apply(HttpSecurity)} from their security config so there is a
 * single source of truth for header hardening.
 *
 * @author niteshjaitwar
 */
public final class SecurityHeadersCustomizer {

    private static final String API_CONTENT_SECURITY_POLICY =
            "default-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'";

    private SecurityHeadersCustomizer() {
    }

    /**
     * Applies the hardened security header policy to a given HttpSecurity
     * builder. Idempotent across chains.
     *
     * @param http HttpSecurity instance from a SecurityFilterChain bean.
     * @return the same HttpSecurity instance for fluent chaining.
     * @throws Exception propagated from HttpSecurity configuration.
     */
    public static HttpSecurity apply(final HttpSecurity http) throws Exception {
        return http.headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                .httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)
                        .preload(true)
                        .maxAgeInSeconds(31536000))
                .referrerPolicy(ref -> ref.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .contentSecurityPolicy(csp -> csp.policyDirectives(API_CONTENT_SECURITY_POLICY))
                .xssProtection(xss -> xss.headerValue(org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                .permissionsPolicyHeader(pp -> pp.policy("geolocation=(), microphone=(), camera=()")));
    }
}
