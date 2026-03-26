package com.nexra.hrms.nexra.modules.hrms.employee.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Returns a standardized JSON response for unauthenticated access.
 */
@Component
public class EmployeeCoreJsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final AuthenticationException authException
    ) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String message = authException == null || authException.getMessage() == null || authException.getMessage().isBlank()
            ? "Authentication is required."
            : authException.getMessage();
        if ("Full authentication is required to access this resource".equals(message)) {
            message = "Authentication is required.";
        }
        response.getWriter().write("""
            {"success":false,"message":"%s","data":null,"timestamp":"%s"}
            """.formatted(message, Instant.now()));
    }
}
