package com.nexra.hrms.nexra.modules.auth.security;

import com.nexra.hrms.nexra.modules.auth.config.AuthProperties;
import com.nexra.hrms.nexra.modules.auth.entity.UserAccount;
import com.nexra.hrms.nexra.modules.auth.entity.UserProductAccess;
import com.nexra.hrms.nexra.modules.auth.repository.UserProductAccessRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Generates and validates JWT access tokens for authenticated principals.
 * Embeds product-scope claims for HRMS and CRM resource server authorization.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final AuthProperties authProperties;
    private final UserProductAccessRepository userProductAccessRepository;

    /**
     * Creates a signed access token with tenant, role, and product-scope claims.
     * Product claims allow HRMS and CRM resource servers to make authorization
     * decisions without querying the auth service on each request.
     *
     * @param userAccount authenticated user account
     * @return signed JWT access token
     */
    @Override
    public String generateAccessToken(final UserAccount userAccount) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds((long) authProperties.getJwt().getAccessTokenMinutes() * 60L);
        Set<String> roleNames = userAccount.getRoles().stream().map(Enum::name).collect(Collectors.toSet());

        List<UserProductAccess> productAccessList = userProductAccessRepository.findByUser(userAccount);
        Set<String> products = productAccessList.stream()
            .map(access -> access.getProduct().name())
            .collect(Collectors.toSet());
        Map<String, String> productRoles = productAccessList.stream()
            .collect(Collectors.toMap(
                access -> access.getProduct().name(),
                access -> access.getProductRole().name()
            ));

        return Jwts.builder()
            .subject(userAccount.getEmail())
            .claim("uid", userAccount.getId().toString())
            .claim("tenant", userAccount.getTenant().getCode())
            .claim("roles", roleNames)
            .claim("products", products)
            .claim("product_roles", productRoles)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(signingKey())
            .compact();
    }

    /**
     * Parses JWT payload and maps claims into principal object.
     * Falls back to empty collections for product claims to ensure
     * backward compatibility with tokens issued before this version.
     *
     * @param token signed JWT token
     * @return resolved principal details
     */
    @Override
    public JwtPrincipal parsePrincipal(final String token) {
        Claims claims = Jwts.parser()
            .verifyWith(signingKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();

        @SuppressWarnings("unchecked")
        Set<String> roles = Set.copyOf((List<String>) claims.get("roles", List.class));

        @SuppressWarnings("unchecked")
        List<String> rawProducts = (List<String>) claims.getOrDefault("products", List.of());
        Set<String> products = new HashSet<>(rawProducts);

        @SuppressWarnings("unchecked")
        Map<String, String> productRoles = (Map<String, String>) claims.getOrDefault("product_roles", Map.of());

        return new JwtPrincipal(
            UUID.fromString(claims.get("uid", String.class)),
            claims.get("tenant", String.class),
            claims.getSubject(),
            roles,
            products,
            productRoles
        );
    }

    /**
     * Builds HMAC signing key from configured secret.
     *
     * @return HMAC signing key
     */
    private SecretKey signingKey() {
        byte[] bytes = authProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }
}
