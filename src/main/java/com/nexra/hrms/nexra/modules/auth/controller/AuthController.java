package com.nexra.hrms.nexra.modules.auth.controller;

import com.nexra.hrms.nexra.modules.auth.dto.request.LinkVerificationRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.LoginRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.OtpVerificationRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.RefreshTokenRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.RegisterRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.VerificationRequest;
import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.modules.auth.dto.request.MfaVerifySetupRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.PasswordResetConfirmRequest;
import com.nexra.hrms.nexra.modules.auth.dto.response.AuthMeResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.AuthSessionResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.MfaEnableResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.MfaSetupResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.TokenPairResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.UserProfileResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.VerificationDispatchResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.VerificationResultResponse;
import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import com.nexra.hrms.nexra.modules.auth.service.AuthService;
import com.nexra.hrms.nexra.common.exception.NexraUnauthorizedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Exposes authentication APIs for registration, login, token refresh, and verification operations.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Slf4j
@Tag(name = "Authentication", description = "Authentication and identity APIs.")
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
    @Operation(summary = "POST /api/v1/auth/register", description = "Processes POST requests for /api/v1/auth/register.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
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
    @Operation(summary = "POST /api/v1/auth/login", description = "Processes POST requests for /api/v1/auth/login.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenPairResponse>> login(@Valid @RequestBody final LoginRequest request) {
        log.info("AuthController() - login() - Login endpoint invoked, tenantCode={}, email={}", request.tenantCode(), maskEmail(request.email()));
        TokenPairResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful.", response));
    }

    @Operation(summary = "GET /api/v1/auth/me", description = "Returns the authenticated user's profile and entitlements.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile returned."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required.")
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthMeResponse>> me(@AuthenticationPrincipal final JwtPrincipal principal) {
        if (principal == null || principal.userId() == null) {
            throw new NexraUnauthorizedException("Authentication is required.");
        }
        return ResponseEntity.ok(ApiResponse.success(
            "Authenticated profile fetched.",
            authService.getCurrentUser(principal.userId())
        ));
    }

    @Operation(summary = "Confirm password reset with OTP")
    @PostMapping("/password/reset/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(@Valid @RequestBody final PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully.", null));
    }

    @Operation(summary = "Begin TOTP MFA setup")
    @PostMapping("/mfa/setup")
    public ResponseEntity<ApiResponse<MfaSetupResponse>> setupMfa(@AuthenticationPrincipal final JwtPrincipal principal) {
        requirePrincipal(principal);
        return ResponseEntity.ok(ApiResponse.success(
            "MFA setup initiated.",
            authService.setupMfa(principal.userId())
        ));
    }

    @Operation(summary = "Verify TOTP and enable MFA")
    @PostMapping("/mfa/verify-setup")
    public ResponseEntity<ApiResponse<MfaEnableResponse>> verifyMfaSetup(
        @AuthenticationPrincipal final JwtPrincipal principal,
        @Valid @RequestBody final MfaVerifySetupRequest request
    ) {
        requirePrincipal(principal);
        return ResponseEntity.ok(ApiResponse.success(
            "MFA enabled successfully.",
            authService.verifyMfaSetup(principal.userId(), request)
        ));
    }

    @Operation(summary = "Disable MFA")
    @PostMapping("/mfa/disable")
    public ResponseEntity<ApiResponse<Void>> disableMfa(
        @AuthenticationPrincipal final JwtPrincipal principal,
        @Valid @RequestBody final MfaVerifySetupRequest request
    ) {
        requirePrincipal(principal);
        authService.disableMfa(principal.userId(), request);
        return ResponseEntity.ok(ApiResponse.success("MFA disabled successfully.", null));
    }

    @Operation(summary = "List active sessions")
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<AuthSessionResponse>>> listSessions(
        @AuthenticationPrincipal final JwtPrincipal principal,
        @RequestParam(required = false) final String currentRefreshToken
    ) {
        requirePrincipal(principal);
        return ResponseEntity.ok(ApiResponse.success(
            "Active sessions listed.",
            authService.listSessions(principal.userId(), currentRefreshToken)
        ));
    }

    @Operation(summary = "Revoke all sessions")
    @PostMapping("/sessions/revoke-all")
    public ResponseEntity<ApiResponse<Void>> revokeAllSessions(
        @AuthenticationPrincipal final JwtPrincipal principal,
        @RequestBody(required = false) final RefreshTokenRequest keepCurrent
    ) {
        requirePrincipal(principal);
        authService.revokeAllSessions(principal.userId(), keepCurrent);
        return ResponseEntity.ok(ApiResponse.success("Sessions revoked successfully.", null));
    }

    /**
     * Rotates refresh token and returns a new token pair.
     *
     * @param request refresh token payload
     * @return standardized API response with rotated token pair
     */
    @Operation(summary = "POST /api/v1/auth/refresh", description = "Processes POST requests for /api/v1/auth/refresh.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenPairResponse>> refreshToken(@Valid @RequestBody final RefreshTokenRequest request) {
        log.info("AuthController() - refreshToken() - Refresh endpoint invoked");
        TokenPairResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully.", response));
    }

    /**
     * Revokes the presented refresh token for explicit logout.
     *
     * @param request refresh token payload
     * @return standardized API response
     */
    @Operation(summary = "POST /api/v1/auth/logout", description = "Processes POST requests for /api/v1/auth/logout.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody final RefreshTokenRequest request) {
        log.info("AuthController() - logout() - Logout endpoint invoked");
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success("Logout successful.", null));
    }

    /**
     * Initiates OTP delivery for account verification or passwordless login.
     *
     * @param request OTP dispatch payload
     * @return standardized API response with delivery metadata
     */
    @Operation(summary = "POST /api/v1/auth/verification/otp/request", description = "Processes POST requests for /api/v1/auth/verification/otp/request.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
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
    @Operation(summary = "POST /api/v1/auth/verification/otp/verify", description = "Processes POST requests for /api/v1/auth/verification/otp/verify.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
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
    @Operation(summary = "POST /api/v1/auth/verification/link/request", description = "Processes POST requests for /api/v1/auth/verification/link/request.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
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
    @Operation(summary = "POST /api/v1/auth/verification/link/verify", description = "Processes POST requests for /api/v1/auth/verification/link/verify.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
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

    private void requirePrincipal(final JwtPrincipal principal) {
        if (principal == null || principal.userId() == null) {
            throw new NexraUnauthorizedException("Authentication is required.");
        }
    }
}
