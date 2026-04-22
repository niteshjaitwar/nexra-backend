package com.nexra.hrms.nexra.modules.hrms.timesheet.config;

import com.nexra.hrms.nexra.modules.hrms.timesheet.security.TimesheetAuthFilter;
import com.nexra.hrms.nexra.modules.hrms.timesheet.security.TimesheetJsonAccessDeniedHandler;
import com.nexra.hrms.nexra.modules.hrms.timesheet.security.TimesheetJsonAuthenticationEntryPoint;
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
 * Configures stateless API security for timesheet endpoints.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(TimesheetProperties.class)
public class TimesheetSecurityConfig {

    @Bean
    @Order(7)
    public SecurityFilterChain timesheetSecurityFilterChain(
        final HttpSecurity http,
        final TimesheetAuthFilter timesheetAuthFilter,
        final TimesheetJsonAuthenticationEntryPoint authenticationEntryPoint,
        final TimesheetJsonAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
            .securityMatcher("/api/v1/timesheet/**")
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/timesheet/status", "/api/v1/timesheet/capabilities").permitAll()
                .anyRequest().authenticated()
            );
        SecurityHeadersCustomizer.apply(http);
        http.addFilterBefore(timesheetAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
