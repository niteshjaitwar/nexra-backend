package com.nexra.hrms.nexra.modules.auth;

import com.jayway.jsonpath.JsonPath;
import com.nexra.hrms.nexra.modules.auth.repository.RefreshTokenRepository;
import com.nexra.hrms.nexra.modules.auth.repository.TenantRepository;
import com.nexra.hrms.nexra.modules.auth.repository.UserAccountRepository;
import com.nexra.hrms.nexra.modules.auth.repository.VerificationTokenRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityNegativeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setup() {
        verificationTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userAccountRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    @Test
    void shouldEnforceNegativeSecurityScenarios() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String tenantCode = "neg" + suffix;
        String email = "neg.user." + suffix + "@nexra.local";

        String tenantPayload = """
            {"code":"%s","name":"Negative Suite Tenant","enterprise":true}
            """.formatted(tenantCode);

        String oauthPayload = """
            {
              "clientId":"neg-client-%s",
              "clientSecret":"super-secret-client-123",
              "clientName":"Negative OAuth Client",
              "redirectUri":"http://localhost:3000/callback",
              "scopes":["profile","hrms.read"]
            }
            """.formatted(suffix);

        mockMvc.perform(post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tenantPayload))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/tenants")
                .with(user("basic-user").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(tenantPayload))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/tenants")
                .with(user("platform-admin").roles("PLATFORM_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(tenantPayload))
            .andExpect(status().isOk());

        String registerPayload = """
            {
              "tenantCode":"%s",
              "email":"%s",
              "password":"Password@123",
              "firstName":"Neg",
              "lastName":"User",
              "accountType":"ENTERPRISE"
            }
            """.formatted(tenantCode, email);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        String invalidLoginPayload = """
            {
              "tenantCode":"%s",
              "email":"%s",
              "password":"WrongPassword@123"
            }
            """.formatted(tenantCode, email);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidLoginPayload))
            .andExpect(status().isUnauthorized());

        String otpRequestPayload = """
            {
              "tenantCode":"%s",
              "email":"%s",
              "purpose":"ACCOUNT_VERIFICATION"
            }
            """.formatted(tenantCode, email);

        MvcResult otpRequestResult = mockMvc.perform(post("/api/v1/auth/verification/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(otpRequestPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andReturn();

        String otpRequestBody = otpRequestResult.getResponse().getContentAsString();
        String otp = JsonPath.read(otpRequestBody, "$.data.rawTokenForDevOnly");

        String invalidOtpVerifyPayload = """
            {
              "tenantCode":"%s",
              "email":"%s",
              "purpose":"ACCOUNT_VERIFICATION",
              "otp":"000000"
            }
            """.formatted(tenantCode, email);

        mockMvc.perform(post("/api/v1/auth/verification/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidOtpVerifyPayload))
            .andExpect(status().isUnauthorized());

        String validOtpVerifyPayload = """
            {
              "tenantCode":"%s",
              "email":"%s",
              "purpose":"ACCOUNT_VERIFICATION",
              "otp":"%s"
            }
            """.formatted(tenantCode, email, otp);

        mockMvc.perform(post("/api/v1/auth/verification/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validOtpVerifyPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("ACCOUNT_VERIFIED"));

        mockMvc.perform(post("/api/v1/auth/verification/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validOtpVerifyPayload))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"refreshToken":"invalid-refresh-token"}
                    """))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/tenants")
                .header("Authorization", "Bearer malformed.token.value")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"code":"badtoken","name":"Bad Token Tenant","enterprise":false}
                    """))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/oauth-clients")
                .with(user("platform-admin").roles("PLATFORM_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(oauthPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/v1/oauth-clients")
                .with(user("platform-admin").roles("PLATFORM_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(oauthPayload))
            .andExpect(status().isConflict());

        mockMvc.perform(get("/api/v1/oauth-clients"))
            .andExpect(status().isUnauthorized());

        String crossTenantLoginPayload = """
            {
              "tenantCode":"nexra",
              "email":"%s",
              "password":"Password@123"
            }
            """.formatted(email);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(crossTenantLoginPayload))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldThrottleRepeatedUnknownAccountLoginAttempts() throws Exception {
        String loginPayload = """
            {
              "tenantCode":"nexra",
              "email":"missing.user@nexra.local",
              "password":"WrongPassword@123"
            }
            """;

        for (int attempt = 0; attempt < 5; attempt++) {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials."));
        }

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.message").value("Too many failed login attempts. Please retry later."));
    }

    @Test
    void shouldReturnGenericOtpDispatchForUnknownAccounts() throws Exception {
        String requestPayload = """
            {
              "tenantCode":"nexra",
              "email":"missing.user@nexra.local",
              "purpose":"ACCOUNT_VERIFICATION"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/verification/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.channel").value("OTP"))
            .andExpect(jsonPath("$.data.hint").value("If the account exists, the verification message will be sent shortly."))
            .andExpect(jsonPath("$.data.rawTokenForDevOnly").value(nullValue()));
    }

    @Test
    void shouldRateLimitOtpRequests() throws Exception {
        String tenantPayload = """
            {"code":"ratelimit","name":"Rate Limit Tenant","enterprise":true}
            """;
        mockMvc.perform(post("/api/v1/tenants")
                .with(user("platform-admin").roles("PLATFORM_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(tenantPayload))
            .andExpect(status().isOk());

        String registerPayload = """
            {
              "tenantCode":"ratelimit",
              "email":"ratelimit.user@nexra.local",
              "password":"Password@123",
              "firstName":"Rate",
              "lastName":"Limit",
              "accountType":"ENTERPRISE"
            }
            """;
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerPayload))
            .andExpect(status().isOk());

        String otpRequestPayload = """
            {
              "tenantCode":"ratelimit",
              "email":"ratelimit.user@nexra.local",
              "purpose":"ACCOUNT_VERIFICATION"
            }
            """;

        for (int attempt = 0; attempt < 5; attempt++) {
            mockMvc.perform(post("/api/v1/auth/verification/otp/request")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(otpRequestPayload))
                .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/v1/auth/verification/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(otpRequestPayload))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.message").value("OTP request limit exceeded. Please retry later."));
    }

    @Test
    void shouldReturnJsonForUnauthenticatedProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/oauth-clients"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Authentication is required."));
    }

    @Test
    void shouldEchoCorrelationIdHeader() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .header("X-Request-Id", "req-auth-test-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"nexra",
                      "email":"missing.user@nexra.local",
                      "password":"WrongPassword@123"
                    }
                    """))
            .andExpect(header().string("X-Request-Id", "req-auth-test-001"));
    }

    @Test
    void shouldAlwaysReturnCorrelationIdHeaderEvenWhenMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"nexra",
                      "email":"missing.user@nexra.local",
                      "password":"WrongPassword@123"
                    }
                    """))
            .andExpect(header().exists("X-Request-Id"));
    }

    @Test
    void shouldRejectWeakPasswordsAtRegistration() throws Exception {
        String tenantPayload = """
            {"code":"validation","name":"Validation Tenant","enterprise":true}
            """;
        mockMvc.perform(post("/api/v1/tenants")
                .with(user("platform-admin").roles("PLATFORM_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(tenantPayload))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"validation",
                      "email":"weak.password@nexra.local",
                      "password":"password123",
                      "firstName":"Weak",
                      "lastName":"Password",
                      "accountType":"ENTERPRISE"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Password must contain upper, lower, digit, and special characters.")))
            .andExpect(jsonPath("$.message", containsString("size must be between 12 and 160")));
    }
}
