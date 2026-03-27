package com.nexra.hrms.nexra.modules.auth.service.impl.notification;

import com.nexra.hrms.nexra.modules.auth.service.notification.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Logs notification intents when mail integration is disabled.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@Slf4j
@ConditionalOnProperty(prefix = "app.auth.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingNotificationService implements NotificationService {

    /**
     * Logs OTP dispatch request without external delivery.
     *
     * @param recipientEmail destination email
     * @param otp verification code
     */
    @Override
    public void sendOtp(final String recipientEmail, final String otp) {
        log.info("LoggingNotificationService() - sendOtp() - Mail disabled, OTP not sent externally, recipientEmail={}",
            maskEmail(recipientEmail));
    }

    /**
     * Logs link dispatch request without external delivery.
     *
     * @param recipientEmail destination email
     * @param token verification token
     */
    @Override
    public void sendVerificationLink(final String recipientEmail, final String token) {
        log.info("LoggingNotificationService() - sendVerificationLink() - Mail disabled, link not sent externally, recipientEmail={}",
            maskEmail(recipientEmail));
    }

    private String maskEmail(final String email) {
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
