package com.nexra.hrms.nexra.modules.hrms.attendance.config;

import com.nexra.hrms.nexra.modules.hrms.attendance.security.AttendanceAuthFilter;
import com.nexra.hrms.nexra.modules.hrms.attendance.security.AttendanceJsonAccessDeniedHandler;
import com.nexra.hrms.nexra.modules.hrms.attendance.security.AttendanceJsonAuthenticationEntryPoint;
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
 * Configures stateless API security for attendance endpoints.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(AttendanceProperties.class)
public class AttendanceSecurityConfig {

    /**
     * Creates the attendance security filter chain under the modular monolith.
     *
     * @param http HttpSecurity builder
     * @param requestCorrelationFilter request correlation filter
     * @param attendanceAuthFilter attendance JWT authentication filter
     * @param authenticationEntryPoint JSON 401 handler
     * @param accessDeniedHandler JSON 403 handler
     * @return configured attendance security chain
     * @throws Exception when security configuration fails
     */
    @Bean
    @Order(5)
    public SecurityFilterChain attendanceSecurityFilterChain(
        final HttpSecurity http,
        final AttendanceAuthFilter attendanceAuthFilter,
        final AttendanceJsonAuthenticationEntryPoint authenticationEntryPoint,
        final AttendanceJsonAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
            .securityMatcher("/api/v1/attendance/**")
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/attendance/status", "/api/v1/attendance/capabilities").permitAll()
                .anyRequest().authenticated()
            );
        SecurityHeadersCustomizer.apply(http);
        http.addFilterBefore(attendanceAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
