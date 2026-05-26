package com.nexra.hrms.nexra.modules.auth.service.security;

import com.nexra.hrms.nexra.modules.auth.config.AuthProperties;
import com.nexra.hrms.nexra.modules.auth.exception.RateLimitExceededException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Provides Redis-backed login lock and OTP throttling controls for distributed deployments.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.auth.security", name = "redis-enabled", havingValue = "true")
public class RedisLoginProtectionService implements LoginProtectionService {

    private final StringRedisTemplate redisTemplate;
    private final AuthProperties authProperties;

    /**
     * Validates whether the provided login key is currently locked in Redis.
     *
     * @param loginKey tenant and principal compound key
     */
    @Override
    public void assertLoginAllowed(final String loginKey) {
        String lockKey = "auth:login:lock:" + loginKey;
        String lockValue = redisTemplate.opsForValue().get(lockKey);
        if (lockValue != null) {
            throw new RateLimitExceededException("Too many failed login attempts. Please retry later.");
        }
    }

    /**
     * Records a login failure and applies distributed lock once threshold is exceeded.
     *
     * @param loginKey tenant and principal compound key
     */
    @Override
    public void recordLoginFailure(final String loginKey) {
        String failKey = "auth:login:fail:" + loginKey;
        Long failures = redisTemplate.opsForValue().increment(failKey);
        if (failures == null) {
            return;
        }

        if (failures == 1L) {
            redisTemplate.expire(failKey, Duration.ofMinutes(authProperties.getSecurity().getLoginFailureWindowMinutes()));
        }

        if (failures >= authProperties.getSecurity().getLoginMaxFailures()) {
            String lockKey = "auth:login:lock:" + loginKey;
            redisTemplate.opsForValue().set(lockKey, "1", Duration.ofMinutes(authProperties.getSecurity().getLoginLockMinutes()));
            redisTemplate.delete(failKey);
        }
    }

    /**
     * Clears distributed login-failure and lock keys after successful authentication.
     *
     * @param loginKey tenant and principal compound key
     */
    @Override
    public void clearLoginFailures(final String loginKey) {
        redisTemplate.delete("auth:login:fail:" + loginKey);
        redisTemplate.delete("auth:login:lock:" + loginKey);
    }

    /**
     * Enforces Redis-backed OTP request throttling.
     *
     * @param requestKey tenant and principal compound key
     */
    @Override
    public void assertOtpRequestAllowed(final String requestKey) {
        String otpKey = "auth:otp:req:" + requestKey;
        Long requests = redisTemplate.opsForValue().increment(otpKey);
        if (requests == null) {
            return;
        }

        if (requests == 1L) {
            redisTemplate.expire(otpKey, Duration.ofMinutes(authProperties.getSecurity().getOtpWindowMinutes()));
        }

        if (requests > authProperties.getSecurity().getOtpRequestLimit()) {
            throw new RateLimitExceededException("OTP request limit exceeded. Please retry later.");
        }
    }

    @Override
    public void assertVerificationAttemptAllowed(final String verificationKey) {
        String failKey = "auth:verify:fail:" + verificationKey;
        String failures = redisTemplate.opsForValue().get(failKey);
        if (failures != null && Long.parseLong(failures) >= authProperties.getSecurity().getOtpRequestLimit()) {
            throw new RateLimitExceededException("Verification attempt limit exceeded. Please retry later.");
        }
    }

    @Override
    public void recordVerificationFailure(final String verificationKey) {
        String failKey = "auth:verify:fail:" + verificationKey;
        Long failures = redisTemplate.opsForValue().increment(failKey);
        if (failures != null && failures == 1L) {
            redisTemplate.expire(failKey, Duration.ofMinutes(authProperties.getSecurity().getOtpWindowMinutes()));
        }
    }

    @Override
    public void clearVerificationFailures(final String verificationKey) {
        redisTemplate.delete("auth:verify:fail:" + verificationKey);
    }
}
