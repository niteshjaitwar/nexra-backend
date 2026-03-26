package com.nexra.hrms.nexra.modules.auth.security;

import com.nexra.hrms.nexra.modules.auth.entity.UserAccount;

/**
 * Defines JWT token generation and parsing operations.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public interface JwtService {

    /**
     * Generates a signed access token for authenticated user.
     *
     * @param userAccount authenticated user
     * @return signed JWT access token
     */
    String generateAccessToken(UserAccount userAccount);

    /**
     * Parses token and extracts principal claims.
     *
     * @param token signed JWT token
     * @return parsed principal
     */
    JwtPrincipal parsePrincipal(String token);
}
