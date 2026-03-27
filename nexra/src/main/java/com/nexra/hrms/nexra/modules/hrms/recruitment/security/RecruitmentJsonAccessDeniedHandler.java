package com.nexra.hrms.nexra.modules.hrms.recruitment.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexra.hrms.nexra.modules.hrms.recruitment.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Writes standardized JSON responses for forbidden recruitment requests.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Component
public class RecruitmentJsonAccessDeniedHandler implements AccessDeniedHandler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void handle(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final AccessDeniedException accessDeniedException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        OBJECT_MAPPER.writeValue(response.getWriter(), ApiResponse.failure("Access is denied."));
    }
}
