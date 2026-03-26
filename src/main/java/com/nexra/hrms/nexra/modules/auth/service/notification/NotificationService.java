package com.nexra.hrms.nexra.modules.auth.service.notification;

/**
 * Sends user-facing authentication verification messages.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public interface NotificationService {

    /**
     * Sends OTP verification message.
     *
     * @param recipientEmail target recipient email
     * @param otp one time password value
     */
    void sendOtp(String recipientEmail, String otp);

    /**
     * Sends verification link message.
     *
     * @param recipientEmail target recipient email
     * @param token verification token value
     */
    void sendVerificationLink(String recipientEmail, String token);
}
