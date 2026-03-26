package com.nexra.hrms.nexra.modules.auth.service.security;

/**
 * Defines lockout and verification request throttling controls for authentication abuse prevention.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public interface LoginProtectionService {

    /**
     * Ensures current login key is not temporarily locked.
     *
     * @param loginKey tenant and principal compound key
     */
    void assertLoginAllowed(String loginKey);

    /**
     * Records failed login attempt and applies lock if threshold exceeded.
     *
     * @param loginKey tenant and principal compound key
     */
    void recordLoginFailure(String loginKey);

    /**
     * Clears failed login tracking after successful authentication.
     *
     * @param loginKey tenant and principal compound key
     */
    void clearLoginFailures(String loginKey);

    /**
     * Enforces OTP request rate limits.
     *
     * @param requestKey tenant and principal compound key
     */
    void assertOtpRequestAllowed(String requestKey);
}
