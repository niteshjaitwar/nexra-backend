package com.nexra.hrms.nexra.modules.hrms.performance.config;

import com.nexra.hrms.nexra.modules.hrms.performance.security.PerformanceAuthFilter;
import com.nexra.hrms.nexra.modules.hrms.performance.security.PerformanceJsonAccessDeniedHandler;
import com.nexra.hrms.nexra.modules.hrms.performance.security.PerformanceJsonAuthenticationEntryPoint;
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
 * Configures stateless API security for performance endpoints.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(PerformanceProperties.class)
public class PerformanceSecurityConfig {

    @Bean
    @Order(9)
    public SecurityFilterChain performanceSecurityFilterChain(
        final HttpSecurity http,
        final PerformanceAuthFilter performanceAuthFilter,
        final PerformanceJsonAuthenticationEntryPoint authenticationEntryPoint,
        final PerformanceJsonAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
            .securityMatcher("/api/v1/performance/**")
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/performance/status", "/api/v1/performance/capabilities").permitAll()
                .anyRequest().authenticated()
            );
        SecurityHeadersCustomizer.apply(http);
        http.addFilterBefore(performanceAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
