package com.nexra.hrms.nexra.modules.payroll.config;

import com.nexra.hrms.nexra.modules.payroll.security.PayrollAuthFilter;
import com.nexra.hrms.nexra.modules.payroll.security.PayrollJsonAccessDeniedHandler;
import com.nexra.hrms.nexra.modules.payroll.security.PayrollJsonAuthenticationEntryPoint;
import com.nexra.hrms.nexra.modules.payroll.security.PayrollRequestCorrelationFilter;
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
 * Configures stateless API security boundaries for payroll and branding endpoints.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(PayrollProperties.class)
public class PayrollSecurityConfig {

    /**
     * Creates payroll module security chain under the modular monolith.
     *
     * @param http HttpSecurity builder
     * @param requestCorrelationFilter request correlation filter
     * @param payrollAuthFilter payroll JWT authentication filter
     * @param authenticationEntryPoint JSON 401 handler
     * @param accessDeniedHandler JSON 403 handler
     * @return configured payroll security chain
     * @throws Exception when security configuration fails
     */
    @Bean
    @Order(4)
    public SecurityFilterChain payrollSecurityFilterChain(
        final HttpSecurity http,
        final PayrollRequestCorrelationFilter requestCorrelationFilter,
        final PayrollAuthFilter payrollAuthFilter,
        final PayrollJsonAuthenticationEntryPoint authenticationEntryPoint,
        final PayrollJsonAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
            .securityMatcher("/api/v1/payroll/**", "/api/v1/branding/**")
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/branding/**").permitAll()
                .requestMatchers("/api/v1/payroll/status", "/api/v1/payroll/capabilities").permitAll()
                .anyRequest().authenticated()
            );
        http.addFilterBefore(requestCorrelationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(payrollAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
