package com.nexra.hrms.nexra.modules.auth.service.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nexra.hrms.nexra.modules.auth.config.AuthProperties;
import com.nexra.hrms.nexra.modules.auth.exception.RateLimitExceededException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Provides in-memory login lock and OTP throttling controls for environments without Redis.
 * Uses Caffeine cache with TTL-based eviction to prevent unbounded memory growth
 * under sustained load, replacing the previous ConcurrentHashMap implementation.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.auth.security", name = "redis-enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryLoginProtectionService implements LoginProtectionService {

    private final AuthProperties authProperties;

    private final Cache<String, FailureState> loginFailures = Caffeine.newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .maximumSize(50_000)
        .build();

    private final Cache<String, Instant> loginLocks = Caffeine.newBuilder()
        .expireAfterWrite(60, TimeUnit.MINUTES)
        .maximumSize(50_000)
        .build();

    private final Cache<String, FailureState> otpRequests = Caffeine.newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .maximumSize(50_000)
        .build();

    private final Cache<String, FailureState> verificationFailures = Caffeine.newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .maximumSize(50_000)
        .build();

    /**
     * Validates whether the provided login key is currently locked.
     *
     * @param loginKey tenant and principal compound key
     */
    @Override
    public void assertLoginAllowed(final String loginKey) {
        Instant lockExpiry = loginLocks.getIfPresent(loginKey);
        if (lockExpiry != null && lockExpiry.isAfter(Instant.now())) {
            throw new RateLimitExceededException("Too many failed login attempts. Please retry later.");
        }
        if (lockExpiry != null && lockExpiry.isBefore(Instant.now())) {
            loginLocks.invalidate(loginKey);
        }
    }

    /**
     * Records a login failure and applies lock duration once threshold is exceeded.
     *
     * @param loginKey tenant and principal compound key
     */
    @Override
    public void recordLoginFailure(final String loginKey) {
        FailureState state = loginFailures.get(loginKey, key -> new FailureState(Instant.now()));
        if (state.windowStart().plusSeconds((long) authProperties.getSecurity().getLoginFailureWindowMinutes() * 60L)
            .isBefore(Instant.now())) {
            loginFailures.put(loginKey, new FailureState(Instant.now()));
            state = loginFailures.getIfPresent(loginKey);
        }
        if (state == null) {
            return;
        }
        int failures = state.counter().incrementAndGet();
        if (failures >= authProperties.getSecurity().getLoginMaxFailures()) {
            loginLocks.put(loginKey, Instant.now().plusSeconds((long) authProperties.getSecurity().getLoginLockMinutes() * 60L));
            loginFailures.invalidate(loginKey);
        }
    }

    /**
     * Clears tracking state after successful authentication.
     *
     * @param loginKey tenant and principal compound key
     */
    @Override
    public void clearLoginFailures(final String loginKey) {
        loginFailures.invalidate(loginKey);
        loginLocks.invalidate(loginKey);
    }

    /**
     * Enforces OTP request rate limits for provided request key.
     *
     * @param requestKey tenant and principal compound key
     */
    @Override
    public void assertOtpRequestAllowed(final String requestKey) {
        FailureState state = otpRequests.get(requestKey, key -> new FailureState(Instant.now()));
        if (state != null && state.windowStart().plusSeconds((long) authProperties.getSecurity().getOtpWindowMinutes() * 60L)
            .isBefore(Instant.now())) {
            otpRequests.put(requestKey, new FailureState(Instant.now()));
            state = otpRequests.getIfPresent(requestKey);
        }
        if (state == null) {
            return;
        }
        int requests = state.counter().incrementAndGet();
        if (requests > authProperties.getSecurity().getOtpRequestLimit()) {
            throw new RateLimitExceededException("OTP request limit exceeded. Please retry later.");
        }
    }

    @Override
    public void assertVerificationAttemptAllowed(final String verificationKey) {
        FailureState state = verificationFailures.getIfPresent(verificationKey);
        if (state == null) {
            return;
        }
        if (state.windowStart().plusSeconds((long) authProperties.getSecurity().getOtpWindowMinutes() * 60L)
            .isBefore(Instant.now())) {
            verificationFailures.invalidate(verificationKey);
            return;
        }
        if (state.counter().get() >= authProperties.getSecurity().getOtpRequestLimit()) {
            throw new RateLimitExceededException("Verification attempt limit exceeded. Please retry later.");
        }
    }

    @Override
    public void recordVerificationFailure(final String verificationKey) {
        FailureState state = verificationFailures.get(verificationKey, key -> new FailureState(Instant.now()));
        if (state != null && state.windowStart().plusSeconds((long) authProperties.getSecurity().getOtpWindowMinutes() * 60L)
            .isBefore(Instant.now())) {
            verificationFailures.put(verificationKey, new FailureState(Instant.now()));
            state = verificationFailures.getIfPresent(verificationKey);
        }
        if (state != null) {
            state.counter().incrementAndGet();
        }
    }

    @Override
    public void clearVerificationFailures(final String verificationKey) {
        verificationFailures.invalidate(verificationKey);
    }

    /**
     * Stores request-window state and request counter.
     *
     * @param windowStart start time of the active rate-limit window
     * @param counter request counter for the window
     */
    private record FailureState(Instant windowStart, AtomicInteger counter) {

        private FailureState(final Instant windowStart) {
            this(windowStart, new AtomicInteger(0));
        }
    }
}
