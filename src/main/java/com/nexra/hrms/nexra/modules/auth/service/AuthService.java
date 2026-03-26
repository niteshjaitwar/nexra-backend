package com.nexra.hrms.nexra.modules.auth.service;

import com.nexra.hrms.nexra.modules.auth.dto.request.LinkVerificationRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.LoginRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.OtpVerificationRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.RefreshTokenRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.RegisterRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.VerificationRequest;
import com.nexra.hrms.nexra.modules.auth.dto.response.TokenPairResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.UserProfileResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.VerificationDispatchResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.VerificationResultResponse;

/**
 * Defines enterprise-grade authentication workflows including credentials, JWT, and verification factors.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public interface AuthService {

    /**
     * Registers a new user account under a tenant and starts account verification state.
     *
     * @param request registration payload
     * @return created user profile
     */
    UserProfileResponse register(RegisterRequest request);

    /**
     * Authenticates by username and password and returns token pair.
     *
     * @param request login payload
     * @return access and refresh token payload
     */
    TokenPairResponse login(LoginRequest request);

    /**
     * Rotates refresh token and returns new token pair.
     *
     * @param request refresh token payload
     * @return new access and refresh token payload
     */
    TokenPairResponse refreshToken(RefreshTokenRequest request);

    /**
     * Generates and dispatches OTP for requested purpose.
     *
     * @param request OTP dispatch request
     * @return dispatch metadata
     */
    VerificationDispatchResponse requestOtp(VerificationRequest request);

    /**
     * Validates OTP and applies requested verification business flow.
     *
     * @param request OTP verification payload
     * @return verification result with optional tokens
     */
    VerificationResultResponse verifyOtp(OtpVerificationRequest request);

    /**
     * Generates and dispatches verification link token.
     *
     * @param request link dispatch request
     * @return dispatch metadata
     */
    VerificationDispatchResponse requestLink(VerificationRequest request);

    /**
     * Validates verification link token and applies requested verification business flow.
     *
     * @param request link verification payload
     * @return verification result with optional tokens
     */
    VerificationResultResponse verifyLink(LinkVerificationRequest request);
}
