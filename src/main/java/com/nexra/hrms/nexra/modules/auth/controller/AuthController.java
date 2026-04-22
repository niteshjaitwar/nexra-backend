package com.nexra.hrms.nexra.modules.auth.controller;

import com.nexra.hrms.nexra.modules.auth.dto.request.LinkVerificationRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.LoginRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.OtpVerificationRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.RefreshTokenRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.RegisterRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.VerificationRequest;
import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.TokenPairResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.UserProfileResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.VerificationDispatchResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.VerificationResultResponse;
import com.nexra.hrms.nexra.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes authentication APIs for registration, login, token refresh, and verification operations.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a tenant user and returns profile details in pending verification state.
     *
     * @param request registration input payload
     * @return standardized API response with created profile
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserProfileResponse>> register(@Valid @RequestBody final RegisterRequest request) {
        log.info("AuthController() - register() - Register endpoint invoked, tenantCode={}, email={}", request.tenantCode(), maskEmail(request.email()));
        UserProfileResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("User registered. Verify account before login.", response));
    }

    /**
     * Authenticates credentials and returns access plus refresh tokens.
     *
     * @param request login input payload
     * @return standardized API response with token pair
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenPairResponse>> login(@Valid @RequestBody final LoginRequest request) {
        log.info("AuthController() - login() - Login endpoint invoked, tenantCode={}, email={}", request.tenantCode(), maskEmail(request.email()));
        TokenPairResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful.", response));
    }

    /**
     * Rotates refresh token and returns a new token pair.
     *
     * @param request refresh token payload
     * @return standardized API response with rotated token pair
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenPairResponse>> refreshToken(@Valid @RequestBody final RefreshTokenRequest request) {
        log.info("AuthController() - refreshToken() - Refresh endpoint invoked");
        TokenPairResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully.", response));
    }

    /**
     * Initiates OTP delivery for account verification or passwordless login.
     *
     * @param request OTP dispatch payload
     * @return standardized API response with delivery metadata
     */
    @PostMapping("/verification/otp/request")
    public ResponseEntity<ApiResponse<VerificationDispatchResponse>> requestOtp(@Valid @RequestBody final VerificationRequest request) {
        log.info("AuthController() - requestOtp() - OTP request endpoint invoked, tenantCode={}, email={}, purpose={}",
            request.tenantCode(), maskEmail(request.email()), request.purpose());
        VerificationDispatchResponse response = authService.requestOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP dispatched.", response));
    }

    /**
     * Validates OTP and executes the requested verification flow.
     *
     * @param request OTP verification payload
     * @return standardized API response with verification result
     */
    @PostMapping("/verification/otp/verify")
    public ResponseEntity<ApiResponse<VerificationResultResponse>> verifyOtp(@Valid @RequestBody final OtpVerificationRequest request) {
        log.info("AuthController() - verifyOtp() - OTP verification endpoint invoked, tenantCode={}, email={}, purpose={}",
            request.tenantCode(), maskEmail(request.email()), request.purpose());
        VerificationResultResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully.", response));
    }

    /**
     * Initiates secure link delivery for account verification or passwordless login.
     *
     * @param request link dispatch payload
     * @return standardized API response with delivery metadata
     */
    @PostMapping("/verification/link/request")
    public ResponseEntity<ApiResponse<VerificationDispatchResponse>> requestLink(@Valid @RequestBody final VerificationRequest request) {
        log.info("AuthController() - requestLink() - Link request endpoint invoked, tenantCode={}, email={}, purpose={}",
            request.tenantCode(), maskEmail(request.email()), request.purpose());
        VerificationDispatchResponse response = authService.requestLink(request);
        return ResponseEntity.ok(ApiResponse.success("Verification link dispatched.", response));
    }

    /**
     * Validates secure link token and executes the requested verification flow.
     *
     * @param request link verification payload
     * @return standardized API response with verification result
     */
    @PostMapping("/verification/link/verify")
    public ResponseEntity<ApiResponse<VerificationResultResponse>> verifyLink(@Valid @RequestBody final LinkVerificationRequest request) {
        log.info("AuthController() - verifyLink() - Link verification endpoint invoked, tenantCode={}, email={}, purpose={}",
            request.tenantCode(), maskEmail(request.email()), request.purpose());
        VerificationResultResponse response = authService.verifyLink(request);
        return ResponseEntity.ok(ApiResponse.success("Verification link validated.", response));
    }
    private String maskEmail(final String email) {
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
