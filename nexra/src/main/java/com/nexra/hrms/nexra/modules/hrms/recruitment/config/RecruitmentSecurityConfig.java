package com.nexra.hrms.nexra.modules.hrms.recruitment.config;

import com.nexra.hrms.nexra.modules.hrms.recruitment.security.RecruitmentAuthFilter;
import com.nexra.hrms.nexra.modules.hrms.recruitment.security.RecruitmentJsonAccessDeniedHandler;
import com.nexra.hrms.nexra.modules.hrms.recruitment.security.RecruitmentJsonAuthenticationEntryPoint;
import com.nexra.hrms.nexra.modules.hrms.recruitment.security.RecruitmentRequestCorrelationFilter;
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
 * Configures stateless API security for recruitment endpoints.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(RecruitmentProperties.class)
public class RecruitmentSecurityConfig {

    @Bean
    @Order(10)
    public SecurityFilterChain recruitmentSecurityFilterChain(
        final HttpSecurity http,
        final RecruitmentRequestCorrelationFilter requestCorrelationFilter,
        final RecruitmentAuthFilter recruitmentAuthFilter,
        final RecruitmentJsonAuthenticationEntryPoint authenticationEntryPoint,
        final RecruitmentJsonAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
            .securityMatcher("/api/v1/recruitment/**")
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/recruitment/status", "/api/v1/recruitment/capabilities").permitAll()
                .anyRequest().authenticated()
            );
        http.addFilterBefore(requestCorrelationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(recruitmentAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
