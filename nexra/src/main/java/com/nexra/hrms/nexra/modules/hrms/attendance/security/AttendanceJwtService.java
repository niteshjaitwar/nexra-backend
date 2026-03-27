package com.nexra.hrms.nexra.modules.hrms.attendance.security;

import com.nexra.hrms.nexra.modules.hrms.attendance.config.AttendanceProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Parses attendance JWT bearer tokens issued by the shared auth domain.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class AttendanceJwtService {

    private final AttendanceProperties attendanceProperties;

    public AuthenticatedAttendanceUser parseBearerToken(final String token) {
        Claims claims = Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload();
        @SuppressWarnings("unchecked") List<String> roles = claims.get("roles", List.class);
        return new AuthenticatedAttendanceUser(
            UUID.fromString(claims.get("uid", String.class)),
            claims.getSubject(),
            claims.get("tenant", String.class),
            roles == null ? Set.of() : Set.copyOf(roles)
        );
    }

    private SecretKey signingKey() {
        byte[] bytes = attendanceProperties.getSecurity().getJwtSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.attendance.security.jwt-secret must be at least 32 bytes");
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}

