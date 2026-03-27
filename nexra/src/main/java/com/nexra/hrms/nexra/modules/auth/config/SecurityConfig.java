package com.nexra.hrms.nexra.modules.auth.config;

import com.nexra.hrms.nexra.modules.auth.security.JwtAuthenticationFilter;
import com.nexra.hrms.nexra.modules.auth.security.JsonAccessDeniedHandler;
import com.nexra.hrms.nexra.modules.auth.security.JsonAuthenticationEntryPoint;
import com.nexra.hrms.nexra.modules.auth.security.RequestCorrelationFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configures application security boundaries, password encoding, and JWT filter integration.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(AuthProperties.class)
public class SecurityConfig {

    /**
     * Creates password encoder used for hashing credentials and client secrets.
     *
     * @return BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures stateless API security chain for non-authorization-server endpoints.
     *
     * @param http HttpSecurity builder
     * @param jwtAuthenticationFilter JWT parsing filter
     * @return configured security filter chain
     * @throws Exception when security configuration fails
     */
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(
        final HttpSecurity http,
        final JwtAuthenticationFilter jwtAuthenticationFilter,
        final RequestCorrelationFilter requestCorrelationFilter,
        final JsonAuthenticationEntryPoint authenticationEntryPoint,
        final JsonAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
            .securityMatcher(
                "/actuator/**",
                "/api/v1/auth/**",
                "/api/v1/admin/**",
                "/api/v1/platform/**",
                "/api/v1/dev/**",
                "/api/v1/tenants/**",
                "/api/v1/oauth-clients/**"
            )
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/v1/auth/register",
                    "/api/v1/auth/login",
                    "/api/v1/auth/refresh",
                    "/api/v1/auth/verification/otp/request",
                    "/api/v1/auth/verification/otp/verify",
                    "/api/v1/auth/verification/link/request",
                    "/api/v1/auth/verification/link/verify"
                ).permitAll()
                .requestMatchers("/api/v1/dev/**").hasRole("PLATFORM_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/tenants/**").hasRole("PLATFORM_ADMIN")
                .requestMatchers("/api/v1/admin/**").hasAnyRole("TENANT_ADMIN", "PLATFORM_ADMIN")
                .requestMatchers("/api/v1/platform/**").hasRole("PLATFORM_ADMIN")
                .anyRequest().authenticated()
            );
        http.addFilterBefore(requestCorrelationFilter, UsernamePasswordAuthenticationFilter.class);
        http
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
