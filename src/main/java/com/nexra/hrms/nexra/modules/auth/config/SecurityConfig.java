package com.nexra.hrms.nexra.modules.auth.config;

import com.nexra.hrms.nexra.modules.auth.security.JwtAuthenticationFilter;
import com.nexra.hrms.nexra.modules.auth.security.JsonAccessDeniedHandler;
import com.nexra.hrms.nexra.modules.auth.security.JsonAuthenticationEntryPoint;
import com.nexra.hrms.nexra.common.web.SecurityHeadersCustomizer;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.stream.Collectors;

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

    @Bean
    public CorsConfigurationSource corsConfigurationSource(final AuthProperties authProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> configuredOrigins = authProperties.getSecurity().getCorsAllowedOrigins();
        List<String> allowedOrigins = (configuredOrigins == null ? List.<String>of() : configuredOrigins)
            .stream()
            .filter(origin -> origin != null && !origin.isBlank())
            .map(String::trim)
            .collect(Collectors.toList());
        if (allowedOrigins.isEmpty()) {
            allowedOrigins = List.of("http://localhost:4200", "http://127.0.0.1:4200");
        }
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "X-Request-Id"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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
                "/api/v1/oauth-clients/**",
                "/api/v1/hrms/**"
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
                    "/api/v1/auth/logout",
                    "/api/v1/auth/verification/otp/request",
                    "/api/v1/auth/verification/otp/verify",
                    "/api/v1/auth/verification/link/request",
                    "/api/v1/auth/verification/link/verify",
                    "/api/v1/auth/password/reset/confirm"
                ).permitAll()
                .requestMatchers("/api/v1/dev/**").hasRole("PLATFORM_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/tenants/**").hasRole("PLATFORM_ADMIN")
                .requestMatchers("/api/v1/admin/**").hasAnyRole("TENANT_ADMIN", "PLATFORM_ADMIN")
                .requestMatchers("/api/v1/platform/**").hasRole("PLATFORM_ADMIN")
                .requestMatchers("/api/v1/hrms/**").authenticated()
                .anyRequest().authenticated()
            );
        SecurityHeadersCustomizer.apply(http);
        http
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
