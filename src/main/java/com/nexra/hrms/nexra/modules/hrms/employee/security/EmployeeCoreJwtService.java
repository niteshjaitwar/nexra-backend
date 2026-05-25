package com.nexra.hrms.nexra.modules.hrms.employee.security;

import com.nexra.hrms.nexra.modules.hrms.employee.config.EmployeeCoreProperties;
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

@Service
@RequiredArgsConstructor
public class EmployeeCoreJwtService {

    private final EmployeeCoreProperties employeeCoreProperties;

    public AuthenticatedEmployeeCoreUser parseBearerToken(final String token) {
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

        return new AuthenticatedEmployeeCoreUser(
            UUID.fromString(claims.get("uid", String.class)),
            claims.getSubject(),
            claims.get("tenant", String.class),
            roles == null ? Set.of() : Set.copyOf(roles)
        );
    }

    private SecretKey signingKey() {
        byte[] bytes = employeeCoreProperties.getSecurity().getJwtSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.employee-core.security.jwt-secret must be at least 32 bytes");
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}
