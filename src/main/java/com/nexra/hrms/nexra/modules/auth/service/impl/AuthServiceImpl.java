package com.nexra.hrms.nexra.modules.auth.service.impl;

import com.nexra.hrms.nexra.modules.auth.config.AuthProperties;
import com.nexra.hrms.nexra.modules.auth.dto.request.LinkVerificationRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.LoginRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.MfaVerifySetupRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.OtpVerificationRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.PasswordResetConfirmRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.RefreshTokenRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.RegisterRequest;
import com.nexra.hrms.nexra.modules.auth.dto.request.VerificationRequest;
import com.nexra.hrms.nexra.modules.auth.dto.response.AuthMeResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.AuthSessionResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.MfaEnableResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.MfaSetupResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.TokenPairResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.UserProfileResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.VerificationDispatchResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.VerificationResultResponse;
import com.nexra.hrms.nexra.modules.auth.entity.RefreshToken;
import com.nexra.hrms.nexra.modules.auth.entity.Tenant;
import com.nexra.hrms.nexra.modules.auth.entity.UserAccount;
import com.nexra.hrms.nexra.modules.auth.entity.UserMfaRecoveryCodeEntity;
import com.nexra.hrms.nexra.modules.auth.entity.VerificationToken;
import com.nexra.hrms.nexra.modules.auth.enums.UserRole;
import com.nexra.hrms.nexra.modules.auth.enums.UserStatus;
import com.nexra.hrms.nexra.modules.auth.enums.VerificationPurpose;
import com.nexra.hrms.nexra.modules.auth.enums.VerificationType;
import com.nexra.hrms.nexra.modules.auth.exception.BusinessException;
import com.nexra.hrms.nexra.modules.auth.exception.UnauthorizedException;
import com.nexra.hrms.nexra.modules.auth.exception.ResourceNotFoundException;
import com.nexra.hrms.nexra.modules.auth.repository.RefreshTokenRepository;
import com.nexra.hrms.nexra.modules.auth.repository.TenantRepository;
import com.nexra.hrms.nexra.modules.auth.repository.UserAccountRepository;
import com.nexra.hrms.nexra.modules.auth.repository.UserMfaRecoveryCodeRepository;
import com.nexra.hrms.nexra.modules.auth.repository.UserProductAccessRepository;
import com.nexra.hrms.nexra.modules.auth.repository.VerificationTokenRepository;
import com.nexra.hrms.nexra.modules.auth.security.JwtService;
import com.nexra.hrms.nexra.modules.auth.service.AuthService;
import com.nexra.hrms.nexra.modules.auth.service.TenantService;
import com.nexra.hrms.nexra.modules.auth.service.notification.NotificationService;
import com.nexra.hrms.nexra.modules.auth.service.security.LoginProtectionService;
import com.nexra.hrms.nexra.modules.auth.service.security.SecurityAuditService;
import com.nexra.hrms.nexra.modules.auth.service.security.TotpService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements enterprise authentication workflows with tenant isolation, token rotation, and verification factors.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String GENERIC_LOGIN_FAILURE_MESSAGE = "Invalid credentials.";
    private static final String GENERIC_VERIFICATION_DISPATCH_HINT = "If the account exists, the verification message will be sent shortly.";
    private final TenantService tenantService;
    private final TenantRepository tenantRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserProductAccessRepository userProductAccessRepository;
    private final UserMfaRecoveryCodeRepository userMfaRecoveryCodeRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthProperties authProperties;
    private final NotificationService notificationService;
    private final LoginProtectionService loginProtectionService;
    private final SecurityAuditService securityAuditService;
    private final TotpService totpService;
    private final ModelMapper modelMapper;

    /**
     * Registers a new user under a specific tenant and marks account for verification.
     *
     * @param request registration payload
     * @return created user profile
     */
    @Override
    @Transactional
    public UserProfileResponse register(final RegisterRequest request) {
        log.info("AuthServiceImpl() - register() - Registering user, tenantCode={}, email={}",
            request.tenantCode(), maskEmail(request.email()));
        Tenant tenant = tenantService.resolveActiveTenant(request.tenantCode());
        if (userAccountRepository.existsByTenantAndEmailIgnoreCase(tenant, request.email())) {
            throw new BusinessException("User already exists for tenant.");
        }

        UserAccount user = modelMapper.map(request, UserAccount.class);
        user.setTenant(tenant);
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setEmailVerified(false);
        user.setMfaEnabled(false);
        user.setRoles(Set.of(UserRole.ROLE_USER));

        UserAccount saved = userAccountRepository.save(user);
        securityAuditService.record("REGISTER", tenant.getCode(), saved.getEmail(), "SUCCESS", "User registered in pending-verification state.");
        log.info("AuthServiceImpl() - register() - User registered successfully, tenantCode={}, email={}",
            tenant.getCode(), maskEmail(saved.getEmail()));
        return toUserProfile(saved);
    }

    /**
     * Authenticates credentials and returns access and refresh tokens.
     *
     * @param request login payload
     * @return token pair response
     */
    @Override
    @Transactional
    public TokenPairResponse login(final LoginRequest request) {
        log.info("AuthServiceImpl() - login() - Processing login, tenantCode={}, email={}",
            request.tenantCode(), maskEmail(request.email()));
        String normalizedTenantCode = normalizeTenantCode(request.tenantCode());
        String normalizedEmail = normalizeEmail(request.email());
        String loginKey = toSecurityKey(normalizedTenantCode, normalizedEmail);
        loginProtectionService.assertLoginAllowed(loginKey);
        UserAccount user = findUser(normalizedTenantCode, normalizedEmail).orElse(null);
        String passwordHash = user == null ? fakePasswordHash() : user.getPasswordHash();
        if (!passwordEncoder.matches(request.password(), passwordHash) || user == null) {
            loginProtectionService.recordLoginFailure(loginKey);
            securityAuditService.record("LOGIN", normalizedTenantCode, normalizedEmail, "FAILURE", "Credential validation failed.");
            throw new UnauthorizedException(GENERIC_LOGIN_FAILURE_MESSAGE);
        }
        if (user.getStatus() != UserStatus.ACTIVE || !user.isEmailVerified()) {
            securityAuditService.record("LOGIN", normalizedTenantCode, normalizedEmail, "FAILURE", "Account not active or email not verified.");
            throw new UnauthorizedException("Account is not verified or inactive.");
        }
        if (user.isMfaEnabled()) {
            final boolean recoveryProvided = request.recoveryCode() != null && !request.recoveryCode().isBlank();
            final boolean totpProvided = request.mfaCode() != null && !request.mfaCode().isBlank();
            if (!recoveryProvided && !totpProvided) {
                securityAuditService.record("LOGIN", normalizedTenantCode, normalizedEmail, "FAILURE", "MFA code missing.");
                throw new UnauthorizedException("MFA code is required.");
            }
            if (recoveryProvided) {
                if (!consumeRecoveryCode(user, request.recoveryCode().trim())) {
                    loginProtectionService.recordLoginFailure(loginKey);
                    securityAuditService.record("LOGIN", normalizedTenantCode, normalizedEmail, "FAILURE", "Invalid MFA recovery code.");
                    throw new UnauthorizedException(GENERIC_LOGIN_FAILURE_MESSAGE);
                }
            } else if (!totpService.verify(request.mfaCode(), user.getMfaSecret())) {
                loginProtectionService.recordLoginFailure(loginKey);
                securityAuditService.record("LOGIN", normalizedTenantCode, normalizedEmail, "FAILURE", "Invalid MFA code.");
                throw new UnauthorizedException(GENERIC_LOGIN_FAILURE_MESSAGE);
            }
        }
        loginProtectionService.clearLoginFailures(loginKey);
        securityAuditService.record("LOGIN", normalizedTenantCode, normalizedEmail, "SUCCESS", "Interactive login completed.");

        return issueTokenPair(user);
    }

    /**
     * Rotates refresh token and returns a fresh token pair.
     *
     * @param request refresh token payload
     * @return token pair response
     */
    @Override
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public TokenPairResponse refreshToken(final RefreshTokenRequest request) {
        log.info("AuthServiceImpl() - refreshToken() - Refreshing token");
        String hash = hash(request.refreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash)
            .orElseThrow(() -> new UnauthorizedException("Refresh token is invalid."));

        if (refreshToken.getRevokedAt() != null) {
            refreshTokenRepository.revokeAllActiveByUser(refreshToken.getUser(), Instant.now());
            securityAuditService.record("REFRESH_TOKEN_REUSE", refreshToken.getUser().getTenant().getCode(), refreshToken.getUser().getEmail(),
                "FAILURE", "Revoked refresh token reuse detected; active sessions revoked.");
            throw new UnauthorizedException("Refresh token is expired or revoked.");
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            securityAuditService.record("REFRESH_TOKEN", refreshToken.getUser().getTenant().getCode(), refreshToken.getUser().getEmail(),
                "FAILURE", "Refresh token expired.");
            throw new UnauthorizedException("Refresh token is expired or revoked.");
        }

        refreshToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);
        securityAuditService.record("REFRESH_TOKEN", refreshToken.getUser().getTenant().getCode(), refreshToken.getUser().getEmail(),
            "SUCCESS", "Refresh token rotated.");

        return issueTokenPair(refreshToken.getUser());
    }

    /**
     * Revokes a refresh token for explicit logout.
     *
     * @param request refresh token payload
     */
    @Override
    @Transactional
    public void logout(final RefreshTokenRequest request) {
        log.info("AuthServiceImpl() - logout() - Revoking refresh token");
        String hash = hash(request.refreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash)
            .orElseThrow(() -> new UnauthorizedException("Refresh token is invalid."));
        if (refreshToken.getRevokedAt() == null) {
            refreshToken.setRevokedAt(Instant.now());
            refreshTokenRepository.save(refreshToken);
            securityAuditService.record("LOGOUT", refreshToken.getUser().getTenant().getCode(), refreshToken.getUser().getEmail(),
                "SUCCESS", "Refresh token revoked by logout.");
            return;
        }
        securityAuditService.record("LOGOUT", refreshToken.getUser().getTenant().getCode(), refreshToken.getUser().getEmail(),
            "IGNORED", "Logout requested for an already revoked refresh token.");
    }

    @Override
    @Transactional(readOnly = true)
    public AuthMeResponse getCurrentUser(final UUID userId) {
        final UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        final Set<String> roleNames = user.getRoles().stream()
            .map(UserRole::name)
            .collect(Collectors.toSet());
        final Set<String> products = userProductAccessRepository.findByUser(user).stream()
            .map((access) -> access.getProduct().name())
            .collect(Collectors.toSet());
        return new AuthMeResponse(
            user.getId(),
            user.getTenant().getCode(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.isEmailVerified(),
            user.isMfaEnabled(),
            roleNames,
            products
        );
    }

    @Override
    @Transactional
    public void confirmPasswordReset(final PasswordResetConfirmRequest request) {
        final UserAccount user = resolveUser(request.tenantCode(), request.email());
        consumeVerificationTokenWithProtection(user, VerificationPurpose.PASSWORD_RESET, VerificationType.OTP, request.otp());
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userAccountRepository.save(user);
        refreshTokenRepository.revokeAllActiveByUser(user, Instant.now());
        securityAuditService.record("PASSWORD_RESET", user.getTenant().getCode(), user.getEmail(), "SUCCESS",
            "Password reset completed and active sessions revoked.");
    }

    @Override
    @Transactional
    public MfaSetupResponse setupMfa(final UUID userId) {
        final UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        if (user.isMfaEnabled()) {
            throw new BusinessException("MFA is already enabled for this account.");
        }
        final String secret = totpService.generateSecret();
        user.setMfaSecret(secret);
        userAccountRepository.save(user);
        final String issuer = user.getTenant().getCode();
        return new MfaSetupResponse(secret, totpService.buildOtpAuthUri(issuer, user.getEmail(), secret));
    }

    @Override
    @Transactional
    public MfaEnableResponse verifyMfaSetup(final UUID userId, final MfaVerifySetupRequest request) {
        final UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        if (user.getMfaSecret() == null || user.getMfaSecret().isBlank()) {
            throw new BusinessException("MFA setup has not been initiated.");
        }
        if (!totpService.verify(request.code(), user.getMfaSecret())) {
            throw new UnauthorizedException("Invalid MFA verification code.");
        }
        user.setMfaEnabled(true);
        userAccountRepository.save(user);
        userMfaRecoveryCodeRepository.deleteAllByUserId(user.getId());
        final List<String> recoveryCodes = generateRecoveryCodes(user);
        securityAuditService.record("MFA_ENABLE", user.getTenant().getCode(), user.getEmail(), "SUCCESS", "TOTP MFA enabled.");
        return new MfaEnableResponse(recoveryCodes);
    }

    @Override
    @Transactional
    public void disableMfa(final UUID userId, final MfaVerifySetupRequest request) {
        final UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        if (!user.isMfaEnabled()) {
            throw new BusinessException("MFA is not enabled for this account.");
        }
        if (!totpService.verify(request.code(), user.getMfaSecret())) {
            throw new UnauthorizedException("Invalid MFA verification code.");
        }
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        userAccountRepository.save(user);
        userMfaRecoveryCodeRepository.deleteAllByUserId(user.getId());
        securityAuditService.record("MFA_DISABLE", user.getTenant().getCode(), user.getEmail(), "SUCCESS", "TOTP MFA disabled.");
    }

    private List<String> generateRecoveryCodes(final UserAccount user) {
        final List<String> plainCodes = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            final String code = randomRecoveryCode();
            plainCodes.add(code);
            final UserMfaRecoveryCodeEntity entity = new UserMfaRecoveryCodeEntity();
            entity.setUserId(user.getId());
            entity.setCodeHash(hash(normalizeRecoveryCode(code)));
            userMfaRecoveryCodeRepository.save(entity);
        }
        return plainCodes;
    }

    private boolean consumeRecoveryCode(final UserAccount user, final String rawCode) {
        return userMfaRecoveryCodeRepository
            .findByUserIdAndCodeHashAndUsedAtIsNull(user.getId(), hash(normalizeRecoveryCode(rawCode)))
            .map((entity) -> {
                entity.setUsedAt(Instant.now());
                userMfaRecoveryCodeRepository.save(entity);
                securityAuditService.record("MFA_RECOVERY_USED", user.getTenant().getCode(), user.getEmail(), "SUCCESS",
                    "MFA recovery code consumed.");
                return true;
            })
            .orElse(false);
    }

    private String randomRecoveryCode() {
        final String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if (i == 4) {
                builder.append('-');
            }
            builder.append(alphabet.charAt(SECURE_RANDOM.nextInt(alphabet.length())));
        }
        return builder.toString();
    }

    private String normalizeRecoveryCode(final String code) {
        return code.trim().toUpperCase(Locale.ROOT).replace("-", "");
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthSessionResponse> listSessions(final UUID userId, final String currentRefreshToken) {
        final UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        final String currentHash = currentRefreshToken == null || currentRefreshToken.isBlank()
            ? null
            : hash(currentRefreshToken.trim());
        return refreshTokenRepository.findByUserAndRevokedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(user, Instant.now())
            .stream()
            .map((session) -> new AuthSessionResponse(
                session.getId(),
                session.getCreatedAt(),
                session.getExpiresAt(),
                currentHash != null && currentHash.equals(session.getTokenHash())
            ))
            .toList();
    }

    @Override
    @Transactional
    public void revokeAllSessions(final UUID userId, final RefreshTokenRequest keepCurrent) {
        final UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        if (keepCurrent == null || keepCurrent.refreshToken() == null || keepCurrent.refreshToken().isBlank()) {
            refreshTokenRepository.revokeAllActiveByUser(user, Instant.now());
            securityAuditService.record("SESSION_REVOKE_ALL", user.getTenant().getCode(), user.getEmail(), "SUCCESS",
                "All active sessions revoked.");
            return;
        }
        final String keepHash = hash(keepCurrent.refreshToken());
        refreshTokenRepository.findByUserAndRevokedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(user, Instant.now())
            .stream()
            .filter((session) -> !keepHash.equals(session.getTokenHash()))
            .forEach((session) -> {
                session.setRevokedAt(Instant.now());
                refreshTokenRepository.save(session);
            });
        securityAuditService.record("SESSION_REVOKE_ALL", user.getTenant().getCode(), user.getEmail(), "SUCCESS",
            "All sessions revoked except current.");
    }

    /**
     * Creates and dispatches OTP token for account verification or passwordless login.
     *
     * @param request verification dispatch payload
     * @return dispatch response
     */
    @Override
    @Transactional
    public VerificationDispatchResponse requestOtp(final VerificationRequest request) {
        log.info("AuthServiceImpl() - requestOtp() - Creating OTP, tenantCode={}, email={}, purpose={}",
            request.tenantCode(), maskEmail(request.email()), request.purpose());
        String normalizedTenantCode = normalizeTenantCode(request.tenantCode());
        String normalizedEmail = normalizeEmail(request.email());
        loginProtectionService.assertOtpRequestAllowed(toSecurityKey(normalizedTenantCode, normalizedEmail));
        UserAccount user = findUser(normalizedTenantCode, normalizedEmail).orElse(null);
        if (user == null) {
            securityAuditService.record("OTP_REQUEST", normalizedTenantCode, normalizedEmail, "IGNORED", "Request accepted with generic response.");
            return genericVerificationDispatch("OTP", maskEmail(normalizedEmail));
        }
        String otp = createOtp();
        persistVerificationToken(user, request.purpose(), VerificationType.OTP, otp);
        notificationService.sendOtp(user.getEmail(), otp);
        securityAuditService.record("OTP_REQUEST", user.getTenant().getCode(), user.getEmail(), "SUCCESS", "OTP dispatched.");

        String destination = maskEmail(user.getEmail());
        return new VerificationDispatchResponse("OTP", destination, "OTP sent successfully.");
    }

    /**
     * Verifies OTP token and executes purpose-specific behavior.
     *
     * @param request OTP verification payload
     * @return verification result
     */
    @Override
    @Transactional
    public VerificationResultResponse verifyOtp(final OtpVerificationRequest request) {
        log.info("AuthServiceImpl() - verifyOtp() - Validating OTP, tenantCode={}, email={}, purpose={}",
            request.tenantCode(), maskEmail(request.email()), request.purpose());
        if (request.purpose() == VerificationPurpose.PASSWORD_RESET) {
            throw new BusinessException("Use POST /api/v1/auth/password/reset/confirm to complete password reset.");
        }
        UserAccount user = resolveUser(request.tenantCode(), request.email());
        consumeVerificationTokenWithProtection(user, request.purpose(), VerificationType.OTP, request.otp());
        securityAuditService.record("OTP_VERIFY", user.getTenant().getCode(), user.getEmail(), "SUCCESS", "OTP verified.");
        return applyVerificationOutcome(user, request.purpose());
    }

    /**
     * Creates and dispatches a secure verification link token.
     *
     * @param request link request payload
     * @return dispatch response
     */
    @Override
    @Transactional
    public VerificationDispatchResponse requestLink(final VerificationRequest request) {
        log.info("AuthServiceImpl() - requestLink() - Creating verification link, tenantCode={}, email={}, purpose={}",
            request.tenantCode(), maskEmail(request.email()), request.purpose());
        String normalizedTenantCode = normalizeTenantCode(request.tenantCode());
        String normalizedEmail = normalizeEmail(request.email());
        loginProtectionService.assertOtpRequestAllowed(toSecurityKey(normalizedTenantCode, normalizedEmail));
        UserAccount user = findUser(normalizedTenantCode, normalizedEmail).orElse(null);
        if (user == null) {
            securityAuditService.record("LINK_REQUEST", normalizedTenantCode, normalizedEmail, "IGNORED", "Request accepted with generic response.");
            return genericVerificationDispatch("EMAIL_LINK", maskEmail(normalizedEmail));
        }
        String linkToken = createLinkToken();
        persistVerificationToken(user, request.purpose(), VerificationType.EMAIL_LINK, linkToken);
        notificationService.sendVerificationLink(user.getEmail(), linkToken);
        securityAuditService.record("LINK_REQUEST", user.getTenant().getCode(), user.getEmail(), "SUCCESS", "Verification link dispatched.");

        String destination = maskEmail(user.getEmail());
        return new VerificationDispatchResponse("EMAIL_LINK", destination, "Verification link sent successfully.");
    }

    /**
     * Verifies secure link token and executes purpose-specific behavior.
     *
     * @param request link verification payload
     * @return verification result
     */
    @Override
    @Transactional
    public VerificationResultResponse verifyLink(final LinkVerificationRequest request) {
        log.info("AuthServiceImpl() - verifyLink() - Validating link token, tenantCode={}, email={}, purpose={}",
            request.tenantCode(), maskEmail(request.email()), request.purpose());
        UserAccount user = resolveUser(request.tenantCode(), request.email());
        consumeVerificationTokenWithProtection(user, request.purpose(), VerificationType.EMAIL_LINK, request.token());
        securityAuditService.record("LINK_VERIFY", user.getTenant().getCode(), user.getEmail(), "SUCCESS", "Verification link validated.");
        return applyVerificationOutcome(user, request.purpose());
    }

    /**
     * Applies post-verification behavior based on verification purpose.
     *
     * @param user user account
     * @param purpose verification purpose
     * @return verification result with optional token pair
     */
    private VerificationResultResponse applyVerificationOutcome(
        final UserAccount user,
        final VerificationPurpose purpose
    ) {
        if (purpose == VerificationPurpose.ACCOUNT_VERIFICATION) {
            user.setEmailVerified(true);
            user.setStatus(UserStatus.ACTIVE);
            userAccountRepository.save(user);
            return new VerificationResultResponse("ACCOUNT_VERIFIED", null);
        }

        TokenPairResponse tokens = issueTokenPair(user);
        return new VerificationResultResponse("LOGIN_SUCCESS", tokens);
    }

    /**
     * Resolves user by tenant code and email address.
     *
     * @param tenantCode tenant unique code
     * @param email user email
     * @return resolved user account
     */
    private UserAccount resolveUser(final String tenantCode, final String email) {
        Tenant tenant = tenantService.resolveActiveTenant(normalizeTenantCode(tenantCode));
        return userAccountRepository.findByTenantAndEmailIgnoreCase(tenant, normalizeEmail(email))
            .orElseThrow(() -> new UnauthorizedException("User not found for tenant."));
    }

    private Optional<UserAccount> findUser(final String tenantCode, final String email) {
        return tenantRepository.findByCodeIgnoreCaseAndActiveTrue(tenantCode)
            .flatMap(tenant -> userAccountRepository.findByTenantAndEmailIgnoreCase(tenant, email));
    }

    /**
     * Issues access and refresh tokens for an authenticated user.
     *
     * @param user authenticated user
     * @return token pair response
     */
    private TokenPairResponse issueTokenPair(final UserAccount user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenRaw = UUID.randomUUID().toString() + UUID.randomUUID();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hash(refreshTokenRaw));
        refreshToken.setExpiresAt(Instant.now().plusSeconds((long) authProperties.getJwt().getRefreshTokenDays() * 86400L));
        refreshTokenRepository.save(refreshToken);
        refreshTokenRepository.deleteByUserAndExpiresAtBefore(user, Instant.now());

        UserProfileResponse profile = toUserProfile(user);
        return new TokenPairResponse(
            accessToken,
            refreshTokenRaw,
            "Bearer",
            (long) authProperties.getJwt().getAccessTokenMinutes() * 60L,
            profile
        );
    }

    /**
     * Maps user entity to API profile response.
     *
     * @param user user account entity
     * @return mapped profile response
     */
    private UserProfileResponse toUserProfile(final UserAccount user) {
        UserProfileResponse mapped = modelMapper.map(user, UserProfileResponse.class);
        mapped.setTenantCode(user.getTenant().getCode());
        return mapped;
    }

    /**
     * Persists verification token hash with delivery and expiry metadata.
     *
     * @param user user account
     * @param purpose verification purpose
     * @param type verification type
     * @param rawToken raw token before hashing
     */
    private void persistVerificationToken(
        final UserAccount user,
        final VerificationPurpose purpose,
        final VerificationType type,
        final String rawToken
    ) {
        verificationTokenRepository.deleteByUserAndExpiresAtBefore(user, Instant.now());
        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setPurpose(purpose);
        token.setType(type);
        token.setTokenHash(hash(rawToken));
        token.setDeliveryTarget(user.getEmail());
        token.setExpiresAt(Instant.now().plusSeconds((long) resolveVerificationExpiryMinutes(type) * 60L));
        verificationTokenRepository.save(token);
    }

    /**
     * Validates and consumes verification token for one-time usage.
     *
     * @param user user account
     * @param purpose verification purpose
     * @param type verification type
     * @param rawToken raw token from request
     */
    private void consumeVerificationToken(
        final UserAccount user,
        final VerificationPurpose purpose,
        final VerificationType type,
        final String rawToken
    ) {
        VerificationToken token = verificationTokenRepository.findByUserAndPurposeAndTypeAndTokenHash(
                user,
                purpose,
                type,
                hash(rawToken)
            )
            .orElseThrow(() -> new UnauthorizedException("Verification token is invalid."));

        if (token.getConsumedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Verification token is expired or already used.");
        }

        token.setConsumedAt(Instant.now());
        verificationTokenRepository.save(token);
    }

    private void consumeVerificationTokenWithProtection(
        final UserAccount user,
        final VerificationPurpose purpose,
        final VerificationType type,
        final String rawToken
    ) {
        String verificationKey = toVerificationKey(user, purpose, type);
        loginProtectionService.assertVerificationAttemptAllowed(verificationKey);
        try {
            consumeVerificationToken(user, purpose, type, rawToken);
            loginProtectionService.clearVerificationFailures(verificationKey);
        } catch (UnauthorizedException exception) {
            loginProtectionService.recordVerificationFailure(verificationKey);
            throw exception;
        }
    }

    /**
     * Resolves verification token expiry configuration by token type.
     *
     * @param type verification type
     * @return expiry in minutes
     */
    private int resolveVerificationExpiryMinutes(final VerificationType type) {
        return type == VerificationType.OTP
            ? authProperties.getOtpExpiryMinutes()
            : authProperties.getLinkExpiryMinutes();
    }

    /**
     * Generates six-digit OTP value.
     *
     * @return OTP string
     */
    private String createOtp() {
        int value = SECURE_RANDOM.nextInt(900000) + 100000;
        return Integer.toString(value);
    }

    /**
     * Generates cryptographically secure token for verification link flow.
     *
     * @return URL-safe token string
     */
    private String createLinkToken() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Produces SHA-256 hash for sensitive token persistence.
     *
     * @param raw raw token value
     * @return hex-encoded hash
     */
    private String hash(final String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", exception);
        }
    }

    /**
     * Masks email to avoid exposing full address in API responses.
     *
     * @param email source email
     * @return masked email value
     */
    private String maskEmail(final String email) {
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }

    /**
     * Returns safe token prefix for structured logging.
     *
     * @param token token value
     * @return first six characters or fewer
     */
    private VerificationDispatchResponse genericVerificationDispatch(final String channel, final String destination) {
        return new VerificationDispatchResponse(channel, destination, GENERIC_VERIFICATION_DISPATCH_HINT);
    }

    private String fakePasswordHash() {
        return "$2a$10$7EqJtq98hPqEX7fNZaFWoO5Q6RDopnczHxVn9Yx8RJWb1x1o1t4bm";
    }

    private String normalizeTenantCode(final String tenantCode) {
        return tenantCode.trim().toLowerCase();
    }

    private String normalizeEmail(final String email) {
        return email.trim().toLowerCase();
    }

    /**
     * Builds compound security key for tenant-scoped lock and throttle tracking.
     *
     * @param tenantCode tenant unique code
     * @param email principal email
     * @return normalized security key
     */
    private String toSecurityKey(final String tenantCode, final String email) {
        return normalizeTenantCode(tenantCode) + ":" + normalizeEmail(email);
    }

    private String toVerificationKey(
        final UserAccount user,
        final VerificationPurpose purpose,
        final VerificationType type
    ) {
        return toSecurityKey(user.getTenant().getCode(), user.getEmail()) + ":" + purpose.name() + ":" + type.name();
    }
}
