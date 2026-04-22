package com.nexra.hrms.nexra.modules.hrms.employee.config;

import com.nexra.hrms.nexra.modules.hrms.employee.security.EmployeeCoreAuthFilter;
import com.nexra.hrms.nexra.modules.hrms.employee.security.EmployeeCoreJsonAccessDeniedHandler;
import com.nexra.hrms.nexra.modules.hrms.employee.security.EmployeeCoreJsonAuthenticationEntryPoint;
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
 * Configures stateless API security for employee-core endpoints.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(EmployeeCoreProperties.class)
public class EmployeeCoreSecurityConfig {

    @Bean
    @Order(3)
    public SecurityFilterChain employeeCoreSecurityFilterChain(
        final HttpSecurity http,
        final EmployeeCoreAuthFilter employeeCoreAuthFilter,
        final EmployeeCoreJsonAuthenticationEntryPoint authenticationEntryPoint,
        final EmployeeCoreJsonAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
            .securityMatcher("/api/v1/employee-core/**")
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/employee-core/status", "/api/v1/employee-core/capabilities").permitAll()
                .anyRequest().authenticated()
            );
        SecurityHeadersCustomizer.apply(http);
        http.addFilterBefore(employeeCoreAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
