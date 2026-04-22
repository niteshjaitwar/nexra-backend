package com.nexra.hrms.nexra.modules.hrms.expense.config;

import com.nexra.hrms.nexra.modules.hrms.expense.security.ExpenseAuthFilter;
import com.nexra.hrms.nexra.modules.hrms.expense.security.ExpenseJsonAccessDeniedHandler;
import com.nexra.hrms.nexra.modules.hrms.expense.security.ExpenseJsonAuthenticationEntryPoint;
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
 * Configures stateless API security for expense endpoints.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(ExpenseProperties.class)
public class ExpenseSecurityConfig {

    @Bean
    @Order(11)
    public SecurityFilterChain expenseSecurityFilterChain(
        final HttpSecurity http,
        final ExpenseAuthFilter expenseAuthFilter,
        final ExpenseJsonAuthenticationEntryPoint authenticationEntryPoint,
        final ExpenseJsonAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
            .securityMatcher("/api/v1/expense/**")
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/expense/status", "/api/v1/expense/capabilities").permitAll()
                .anyRequest().authenticated()
            );
        SecurityHeadersCustomizer.apply(http);
        http.addFilterBefore(expenseAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
