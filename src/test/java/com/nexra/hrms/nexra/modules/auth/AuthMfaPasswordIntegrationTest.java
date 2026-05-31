package com.nexra.hrms.nexra.modules.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.nexra.hrms.nexra.modules.auth.repository.RefreshTokenRepository;
import com.nexra.hrms.nexra.modules.auth.repository.TenantRepository;
import com.nexra.hrms.nexra.modules.auth.repository.UserAccountRepository;
import com.nexra.hrms.nexra.modules.auth.repository.UserMfaRecoveryCodeRepository;
import com.nexra.hrms.nexra.modules.auth.repository.VerificationTokenRepository;
import com.nexra.hrms.nexra.modules.auth.service.security.TotpService;
import com.nexra.hrms.nexra.modules.auth.support.CapturingNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthMfaPasswordIntegrationTest {

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

    @Autowired
    private UserMfaRecoveryCodeRepository userMfaRecoveryCodeRepository;

    @Autowired
    private TotpService totpService;

    @Autowired
    private CapturingNotificationService notificationCapture;

    @BeforeEach
    void setup() {
        verificationTokenRepository.deleteAll();
        userMfaRecoveryCodeRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        notificationCapture.clear();
        userAccountRepository.deleteAll();
        tenantRepository.findByCodeIgnoreCaseAndActiveTrue("mfaco").ifPresent(tenantRepository::delete);
    }

    @Test
    void passwordResetRevokesSessionsAndAllowsNewLogin() throws Exception {
        provisionVerifiedUser("mfaco", "reset@mfaco.com", "Password@123");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"tenantCode":"mfaco","email":"reset@mfaco.com","password":"Password@123"}
                    """))
            .andExpect(status().isOk())
            .andReturn();
        String refreshToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.data.refreshToken");

        mockMvc.perform(post("/api/v1/auth/verification/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"tenantCode":"mfaco","email":"reset@mfaco.com","purpose":"PASSWORD_RESET"}
                    """))
            .andExpect(status().isOk());
        String otp = notificationCapture.lastOtp("reset@mfaco.com");

        mockMvc.perform(post("/api/v1/auth/password/reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"mfaco",
                      "email":"reset@mfaco.com",
                      "otp":"%s",
                      "newPassword":"NewPassword@456"
                    }
                    """.formatted(otp)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"tenantCode":"mfaco","email":"reset@mfaco.com","password":"NewPassword@456"}
                    """))
            .andExpect(status().isOk());
    }

    @Test
    void mfaSetupEnforcesCodeOnLogin() throws Exception {
        provisionVerifiedUser("mfaco", "mfa@mfaco.com", "Password@123");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"tenantCode":"mfaco","email":"mfa@mfaco.com","password":"Password@123"}
                    """))
            .andExpect(status().isOk())
            .andReturn();
        String accessToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.data.accessToken");
        String refreshToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.data.refreshToken");

        MvcResult setupResult = mockMvc.perform(post("/api/v1/auth/mfa/setup")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andReturn();
        String secret = JsonPath.read(setupResult.getResponse().getContentAsString(), "$.data.secret");
        String code = totpService.currentCode(secret);

        mockMvc.perform(post("/api/v1/auth/mfa/verify-setup")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + code + "\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"tenantCode":"mfaco","email":"mfa@mfaco.com","password":"Password@123"}
                    """))
            .andExpect(status().isUnauthorized());

        String loginCode = totpService.currentCode(secret);
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"mfaco",
                      "email":"mfa@mfaco.com",
                      "password":"Password@123",
                      "mfaCode":"%s"
                    }
                    """.formatted(loginCode)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/auth/sessions")
                .header("Authorization", "Bearer " + accessToken)
                .param("currentRefreshToken", refreshToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.current == true)]").exists());
    }

    @Test
    void mfaRecoveryCodeAllowsOneTimeLogin() throws Exception {
        provisionVerifiedUser("mfaco", "recovery@mfaco.com", "Password@123");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"tenantCode":"mfaco","email":"recovery@mfaco.com","password":"Password@123"}
                    """))
            .andExpect(status().isOk())
            .andReturn();
        String accessToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.data.accessToken");

        MvcResult setupResult = mockMvc.perform(post("/api/v1/auth/mfa/setup")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andReturn();
        String secret = JsonPath.read(setupResult.getResponse().getContentAsString(), "$.data.secret");
        String code = totpService.currentCode(secret);

        MvcResult enableResult = mockMvc.perform(post("/api/v1/auth/mfa/verify-setup")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + code + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.recoveryCodes.length()").value(8))
            .andReturn();
        String recoveryCode = JsonPath.read(enableResult.getResponse().getContentAsString(), "$.data.recoveryCodes[0]");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"mfaco",
                      "email":"recovery@mfaco.com",
                      "password":"Password@123",
                      "recoveryCode":"%s"
                    }
                    """.formatted(recoveryCode)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"mfaco",
                      "email":"recovery@mfaco.com",
                      "password":"Password@123",
                      "recoveryCode":"%s"
                    }
                    """.formatted(recoveryCode)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void revokeAllSessionsKeepsCurrentRefreshToken() throws Exception {
        provisionVerifiedUser("mfaco", "sessions@mfaco.com", "Password@123");

        MvcResult firstLogin = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"tenantCode":"mfaco","email":"sessions@mfaco.com","password":"Password@123"}
                    """))
            .andExpect(status().isOk())
            .andReturn();
        String keepAccessToken = JsonPath.read(firstLogin.getResponse().getContentAsString(), "$.data.accessToken");
        String keepRefreshToken = JsonPath.read(firstLogin.getResponse().getContentAsString(), "$.data.refreshToken");

        MvcResult secondLogin = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"tenantCode":"mfaco","email":"sessions@mfaco.com","password":"Password@123"}
                    """))
            .andExpect(status().isOk())
            .andReturn();
        String otherRefreshToken = JsonPath.read(secondLogin.getResponse().getContentAsString(), "$.data.refreshToken");

        mockMvc.perform(post("/api/v1/auth/sessions/revoke-all")
                .header("Authorization", "Bearer " + keepAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + keepRefreshToken + "\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + keepRefreshToken + "\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + otherRefreshToken + "\"}"))
            .andExpect(status().isUnauthorized());
    }

    private void provisionVerifiedUser(final String tenantCode, final String email, final String password) throws Exception {
        mockMvc.perform(post("/api/v1/tenants")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("platform-admin").roles("PLATFORM_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"code":"%s","name":"MFA Co","enterprise":true}
                    """.formatted(tenantCode)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"%s",
                      "email":"%s",
                      "password":"%s",
                      "firstName":"Test",
                      "lastName":"User",
                      "accountType":"ENTERPRISE"
                    }
                    """.formatted(tenantCode, email, password)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/verification/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"tenantCode":"%s","email":"%s","purpose":"ACCOUNT_VERIFICATION"}
                    """.formatted(tenantCode, email)))
            .andExpect(status().isOk());
        String otp = notificationCapture.lastOtp(email);

        mockMvc.perform(post("/api/v1/auth/verification/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"%s",
                      "email":"%s",
                      "purpose":"ACCOUNT_VERIFICATION",
                      "otp":"%s"
                    }
                    """.formatted(tenantCode, email, otp)))
            .andExpect(status().isOk());
    }
}
