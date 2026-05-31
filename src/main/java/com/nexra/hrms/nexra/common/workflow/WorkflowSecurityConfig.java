package com.nexra.hrms.nexra.common.workflow;

import com.nexra.hrms.nexra.common.web.SecurityHeadersCustomizer;
import com.nexra.hrms.nexra.modules.auth.security.JsonAccessDeniedHandler;
import com.nexra.hrms.nexra.modules.auth.security.JsonAuthenticationEntryPoint;
import com.nexra.hrms.nexra.modules.auth.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class WorkflowSecurityConfig {

    @Bean
    @Order(11)
    public SecurityFilterChain workflowSecurityFilterChain(
        final HttpSecurity http,
        final JwtAuthenticationFilter jwtAuthenticationFilter,
        final JsonAuthenticationEntryPoint authenticationEntryPoint,
        final JsonAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
            .securityMatcher("/api/v1/workflow/**")
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
        SecurityHeadersCustomizer.apply(http);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
