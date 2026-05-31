package com.nexra.hrms.nexra.modules.auth.support;

import com.nexra.hrms.nexra.modules.auth.service.notification.NotificationService;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Test-only notification service that captures the most recent OTP and
 * verification link per recipient so integration tests can assert end-to-end
 * verification flows without exposing raw tokens through the production API.
 *
 * <p>Registered as {@link Primary} so it overrides the runtime
 * {@code LoggingNotificationService}/{@code EmailNotificationService} beans
 * inside the test application context.
 */
@Service
@Primary
public class CapturingNotificationService implements NotificationService {

    private final Map<String, String> otpByEmail = new ConcurrentHashMap<>();
    private final Map<String, String> linkByEmail = new ConcurrentHashMap<>();

    @Override
    public void sendOtp(final String recipientEmail, final String otp) {
        otpByEmail.put(normalize(recipientEmail), otp);
    }

    @Override
    public void sendVerificationLink(final String recipientEmail, final String token) {
        linkByEmail.put(normalize(recipientEmail), token);
    }

    /**
     * Returns the most recent OTP captured for the recipient, or {@code null} if none.
     *
     * @param recipientEmail destination email
     * @return last captured OTP
     */
    public String lastOtp(final String recipientEmail) {
        return otpByEmail.get(normalize(recipientEmail));
    }

    /**
     * Returns the most recent verification link token captured for the recipient.
     *
     * @param recipientEmail destination email
     * @return last captured link token
     */
    public String lastLink(final String recipientEmail) {
        return linkByEmail.get(normalize(recipientEmail));
    }

    /**
     * Clears all captured tokens between tests.
     */
    public void clear() {
        otpByEmail.clear();
        linkByEmail.clear();
    }

    private String normalize(final String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
