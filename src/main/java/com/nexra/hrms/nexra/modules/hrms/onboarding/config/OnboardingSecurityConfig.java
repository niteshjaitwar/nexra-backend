package com.nexra.hrms.nexra.modules.hrms.onboarding.config;

import com.nexra.hrms.nexra.modules.hrms.onboarding.security.OnboardingAuthFilter;
import com.nexra.hrms.nexra.modules.hrms.onboarding.security.OnboardingJsonAccessDeniedHandler;
import com.nexra.hrms.nexra.modules.hrms.onboarding.security.OnboardingJsonAuthenticationEntryPoint;
import com.nexra.hrms.nexra.common.web.SecurityHeadersCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configures stateless API security for onboarding endpoints.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(OnboardingProperties.class)
public class OnboardingSecurityConfig {

    @Bean
    @Order(8)
    public SecurityFilterChain onboardingSecurityFilterChain(
        final HttpSecurity http,
        final OnboardingAuthFilter onboardingAuthFilter,
        final OnboardingJsonAuthenticationEntryPoint authenticationEntryPoint,
        final OnboardingJsonAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
            .securityMatcher("/api/v1/onboarding/**")
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/onboarding/status", "/api/v1/onboarding/capabilities").permitAll()
                .anyRequest().authenticated()
            );
        SecurityHeadersCustomizer.apply(http);
        http.addFilterBefore(onboardingAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
