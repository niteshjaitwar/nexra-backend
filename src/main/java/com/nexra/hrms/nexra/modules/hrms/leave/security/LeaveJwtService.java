package com.nexra.hrms.nexra.modules.hrms.leave.security;

import com.nexra.hrms.nexra.modules.hrms.leave.config.LeaveProperties;
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
 * Parses leave JWT bearer tokens issued by the shared auth domain.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class LeaveJwtService {

    private final LeaveProperties leaveProperties;

    public AuthenticatedLeaveUser parseBearerToken(final String token) {
        Claims claims = Jwts.parser()
            .verifyWith(signingKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();

        @SuppressWarnings("unchecked")
        List<String> products = claims.get("products", List.class);
        if (products == null || !products.contains("HRMS")) {
            throw new io.jsonwebtoken.security.SignatureException("User does not have HRMS product access.");
        }

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);

        return new AuthenticatedLeaveUser(
            UUID.fromString(claims.get("uid", String.class)),
            claims.getSubject(),
            claims.get("tenant", String.class),
            roles == null ? Set.of() : Set.copyOf(roles)
        );
    }

    private SecretKey signingKey() {
        byte[] bytes = leaveProperties.getSecurity().getJwtSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.leave.security.jwt-secret must be at least 32 bytes");
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}

