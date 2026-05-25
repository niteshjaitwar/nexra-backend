package com.nexra.hrms.nexra.modules.auth.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Binds application authentication configuration used by JWT, OTP, OAuth2, mail, and security controls.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@ConfigurationProperties(prefix = "app.auth")
@Validated
@Getter
@Setter
public class AuthProperties {

    @Valid
    private Jwt jwt = new Jwt();
    @Valid
    private Security security = new Security();
    @Valid
    private Cookie cookie = new Cookie();
    @Valid
    private Oauth2 oauth2 = new Oauth2();
    @Valid
    private Mail mail = new Mail();
    @Min(1)
    private int otpExpiryMinutes;
    @Min(1)
    private int linkExpiryMinutes;
    private boolean exposeVerificationTokenInResponse;

    /**
     * Encapsulates JWT signing and lifetime properties.
     *
     * @author niteshjaitwar
     * @version 1.0
     */
    @Getter
    @Setter
    public static class Jwt {

        private String secret;
        @Min(1)
        private int accessTokenMinutes;
        @Min(1)
        private int refreshTokenDays;
    }

    /**
     * Encapsulates brute-force and OTP throttling configuration.
     *
     * @author niteshjaitwar
     * @version 1.0
     */
    @Getter
    @Setter
    public static class Security {

        @Min(1)
        private int loginMaxFailures;
        @Min(1)
        private int loginLockMinutes;
        @Min(1)
        private int loginFailureWindowMinutes;
        @Min(1)
        private int otpRequestLimit;
        @Min(1)
        private int otpWindowMinutes;
        private boolean redisEnabled;
        private List<String> corsAllowedOrigins = List.of(
            "http://localhost:4200",
            "http://127.0.0.1:4200",
            "http://localhost:5173",
            "http://127.0.0.1:5173"
        );
    }

    /**
     * Encapsulates browser cookie settings for refresh token transport.
     *
     * @author niteshjaitwar
     * @version 1.0
     */
    @Getter
    @Setter
    public static class Cookie {
        private String refreshTokenName = "nexra_refresh_token";
        private String sameSite = "Lax";
        private boolean secure = false;
        private String path = "/";
    }

    /**
     * Encapsulates OAuth2 issuer and default client bootstrap configuration.
     *
     * @author niteshjaitwar
     * @version 1.0
     */
    @Getter
    @Setter
    public static class Oauth2 {

        private String issuer;
        private String defaultClientId;
        private String defaultClientSecret;
        private String defaultRedirectUri;
        private boolean ephemeralKeyEnabled;
        private String keystoreLocation;
        private String keystorePassword;
        private String keystoreKeyAlias;
        private String keystoreKeyPassword;
    }

    /**
     * Encapsulates notification channel mail properties.
     *
     * @author niteshjaitwar
     * @version 1.0
     */
    @Getter
    @Setter
    public static class Mail {

        private boolean enabled;
        private String from;
    }
}
