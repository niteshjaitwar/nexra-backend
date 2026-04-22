package com.nexra.hrms.nexra.modules.hrms.leave.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Returns a standardized JSON response for forbidden leave access.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Component
public class LeaveJsonAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
            {"success":false,"message":"Access denied.","data":null,"timestamp":"%s"}
            """.formatted(Instant.now()));
    }
}
