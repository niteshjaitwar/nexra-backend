package com.nexra.hrms.nexra.modules.hrms.attendance.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Parses bearer tokens for attendance routes and exposes the authenticated user.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceAuthFilter extends OncePerRequestFilter {

    public static final String ATTR_AUTH_USER = "attendance.authUser";
    private static final String PREFIX = "/api/v1/attendance";

    private final AttendanceJwtService attendanceJwtService;
    private final AttendanceJsonAuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !uri.startsWith(PREFIX) || uri.equals(PREFIX + "/status") || uri.equals(PREFIX + "/capabilities") || uri.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
        throws ServletException, IOException {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            commenceUnauthorized(request, response, "Authentication is required.");
            return;
        }

        try {
            AuthenticatedAttendanceUser authUser = attendanceJwtService.parseBearerToken(auth.substring(7));
            request.setAttribute(ATTR_AUTH_USER, authUser);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                authUser,
                null,
                authUser.roles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Invalid attendance bearer token: {}", ex.getMessage());
            commenceUnauthorized(request, response, "Invalid or expired token.");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void commenceUnauthorized(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final String message
    ) throws IOException, ServletException {
        AuthenticationException exception = new BadCredentialsException(message);
        authenticationEntryPoint.commence(request, response, exception);
    }
}

