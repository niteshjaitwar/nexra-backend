package com.nexra.hrms.nexra.modules.hrms.expense.security;

import com.nexra.hrms.nexra.modules.hrms.expense.config.ExpenseProperties;
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
public class ExpenseJwtService {
    private final ExpenseProperties properties;
    public AuthenticatedExpenseUser parseBearerToken(final String token) {
        Claims claims = Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload();
        @SuppressWarnings("unchecked") List<String> roles = claims.get("roles", List.class);
        return new AuthenticatedExpenseUser(UUID.fromString(claims.get("uid", String.class)), claims.getSubject(),
            claims.get("tenant", String.class), roles == null ? Set.of() : Set.copyOf(roles));
    }
    private SecretKey signingKey() {
        byte[] bytes = properties.getSecurity().getJwtSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalStateException("app.expense.security.jwt-secret must be at least 32 bytes");
        return Keys.hmacShaKeyFor(bytes);
    }
}

