package com.nexra.hrms.nexra.modules.auth.service.impl.notification;

import com.nexra.hrms.nexra.modules.auth.config.AuthProperties;
import com.nexra.hrms.nexra.modules.auth.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Delivers OTP and verification links through SMTP.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.auth.mail", name = "enabled", havingValue = "true")
public class EmailNotificationService implements NotificationService {

    private final JavaMailSender mailSender;
    private final AuthProperties authProperties;

    /**
     * Sends one-time-password over configured email transport.
     *
     * @param recipientEmail destination email
     * @param otp verification code
     */
    @Override
    public void sendOtp(final String recipientEmail, final String otp) {
        log.info("EmailNotificationService() - sendOtp() - Sending OTP email, recipientEmail={}", maskEmail(recipientEmail));
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(authProperties.getMail().getFrom());
        message.setTo(recipientEmail);
        message.setSubject("Nexra HRMS OTP Verification");
        message.setText("Your OTP for Nexra HRMS authentication is: " + otp + "\nThis code expires shortly.");
        mailSender.send(message);
    }

    /**
     * Sends verification link token over configured email transport.
     *
     * @param recipientEmail destination email
     * @param token link token
     */
    @Override
    public void sendVerificationLink(final String recipientEmail, final String token) {
        log.info("EmailNotificationService() - sendVerificationLink() - Sending verification link email, recipientEmail={}",
            maskEmail(recipientEmail));
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(authProperties.getMail().getFrom());
        message.setTo(recipientEmail);
        message.setSubject("Nexra HRMS Verification Link");
        message.setText("Use this verification token for Nexra HRMS: " + token + "\nDo not share this token.");
        mailSender.send(message);
    }

    private String maskEmail(final String email) {
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
