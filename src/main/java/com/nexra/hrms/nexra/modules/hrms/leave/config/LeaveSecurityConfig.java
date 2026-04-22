package com.nexra.hrms.nexra.modules.hrms.leave.config;

import com.nexra.hrms.nexra.modules.hrms.leave.security.LeaveAuthFilter;
import com.nexra.hrms.nexra.modules.hrms.leave.security.LeaveJsonAccessDeniedHandler;
import com.nexra.hrms.nexra.modules.hrms.leave.security.LeaveJsonAuthenticationEntryPoint;
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
 * Configures stateless API security for leave endpoints.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(LeaveProperties.class)
public class LeaveSecurityConfig {

    /**
     * Creates the leave security chain under the modular monolith.
     *
     * @param http HttpSecurity builder
     * @param requestCorrelationFilter request correlation filter
     * @param leaveAuthFilter leave JWT authentication filter
     * @param authenticationEntryPoint JSON 401 handler
     * @param accessDeniedHandler JSON 403 handler
     * @return configured leave security chain
     * @throws Exception when security configuration fails
     */
    @Bean
    @Order(6)
    public SecurityFilterChain leaveSecurityFilterChain(
        final HttpSecurity http,
        final LeaveAuthFilter leaveAuthFilter,
        final LeaveJsonAuthenticationEntryPoint authenticationEntryPoint,
        final LeaveJsonAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
            .securityMatcher("/api/v1/leave/**")
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/leave/status", "/api/v1/leave/capabilities").permitAll()
                .anyRequest().authenticated()
            );
        SecurityHeadersCustomizer.apply(http);
        http.addFilterBefore(leaveAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
