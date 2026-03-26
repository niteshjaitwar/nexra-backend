package com.nexra.hrms.nexra.modules.auth;

import com.nexra.hrms.nexra.modules.auth.repository.TenantRepository;
import com.nexra.hrms.nexra.modules.auth.repository.UserAccountRepository;
import com.jayway.jsonpath.JsonPath;
import com.nexra.hrms.nexra.modules.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import com.nexra.hrms.nexra.modules.auth.repository.VerificationTokenRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

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
        tenantRepository.findByCodeIgnoreCaseAndActiveTrue("acme").ifPresent(tenantRepository::delete);
    }

    @Test
    void shouldCreateTenantSuccessfully() throws Exception {
        String payload = """
            {"code":"acme","name":"Acme Corp","enterprise":true}
            """;

        mockMvc.perform(post("/api/v1/tenants")
                .with(user("platform-admin").roles("PLATFORM_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.code").value("acme"));
    }

    @Test
    void shouldRejectTenantCreationWithoutPlatformAdminRole() throws Exception {
        String payload = """
            {"code":"acme","name":"Acme Corp","enterprise":true}
            """;

        mockMvc.perform(post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldCompleteEndToEndAuthenticationFlowSuccessfully() throws Exception {
        String tenantPayload = """
            {"code":"acme","name":"Acme Corp","enterprise":true}
            """;
        mockMvc.perform(post("/api/v1/tenants")
            .with(user("platform-admin").roles("PLATFORM_ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(tenantPayload)).andExpect(status().isOk());

        String registerPayload = """
            {
              "tenantCode":"acme",
              "email":"user@acme.com",
              "password":"Password@123",
              "firstName":"Nitesh",
              "lastName":"Jaitwar",
              "accountType":"ENTERPRISE"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.email").value("user@acme.com"));

        String otpRequestPayload = """
            {
              "tenantCode":"acme",
              "email":"user@acme.com",
              "purpose":"ACCOUNT_VERIFICATION"
            }
            """;

        MvcResult otpRequestResult = mockMvc.perform(post("/api/v1/auth/verification/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(otpRequestPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andReturn();

        String otpRequestBody = otpRequestResult.getResponse().getContentAsString();
        String otp = JsonPath.read(otpRequestBody, "$.data.rawTokenForDevOnly");

        String otpVerifyPayload = """
            {
              "tenantCode":"acme",
              "email":"user@acme.com",
              "purpose":"ACCOUNT_VERIFICATION",
              "otp":"%s"
            }
            """.formatted(otp);
        mockMvc.perform(post("/api/v1/auth/verification/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(otpVerifyPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("ACCOUNT_VERIFIED"));

        String loginPayload = """
            {
              "tenantCode":"acme",
              "email":"user@acme.com",
              "password":"Password@123"
            }
            """;
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
            .andReturn();

        String loginBody = loginResult.getResponse().getContentAsString();
        String refreshToken = JsonPath.read(loginBody, "$.data.refreshToken");

        String refreshPayload = """
            {
              "refreshToken":"%s"
            }
            """.formatted(refreshToken);
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        String linkRequestPayload = """
            {
              "tenantCode":"acme",
              "email":"user@acme.com",
              "purpose":"LOGIN_PASSWORDLESS"
            }
            """;
        MvcResult linkRequestResult = mockMvc.perform(post("/api/v1/auth/verification/link/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(linkRequestPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andReturn();

        String linkRequestBody = linkRequestResult.getResponse().getContentAsString();
        String linkToken = JsonPath.read(linkRequestBody, "$.data.rawTokenForDevOnly");

        String linkVerifyPayload = """
            {
              "tenantCode":"acme",
              "email":"user@acme.com",
              "purpose":"LOGIN_PASSWORDLESS",
              "token":"%s"
            }
            """.formatted(linkToken);
        mockMvc.perform(post("/api/v1/auth/verification/link/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(linkVerifyPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("LOGIN_SUCCESS"))
            .andExpect(jsonPath("$.data.tokens.accessToken").isNotEmpty());

        mockMvc.perform(get("/api/v1/oauth-clients"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectOAuthClientListingForAuthenticatedNonPlatformUser() throws Exception {
        String tenantPayload = """
            {"code":"oauthdeny","name":"OAuth Deny Tenant","enterprise":true}
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
                      "tenantCode":"oauthdeny",
                      "email":"oauth.deny@nexra.local",
                      "password":"Password@123",
                      "firstName":"O",
                      "lastName":"Auth",
                      "accountType":"ENTERPRISE"
                    }
                    """))
            .andExpect(status().isOk());

        String otpRequestPayload = """
            {
              "tenantCode":"oauthdeny",
              "email":"oauth.deny@nexra.local",
              "purpose":"ACCOUNT_VERIFICATION"
            }
            """;
        MvcResult otpRequestResult = mockMvc.perform(post("/api/v1/auth/verification/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(otpRequestPayload))
            .andExpect(status().isOk())
            .andReturn();
        String otp = JsonPath.read(otpRequestResult.getResponse().getContentAsString(), "$.data.rawTokenForDevOnly");

        mockMvc.perform(post("/api/v1/auth/verification/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"oauthdeny",
                      "email":"oauth.deny@nexra.local",
                      "purpose":"ACCOUNT_VERIFICATION",
                      "otp":"%s"
                    }
                    """.formatted(otp)))
            .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"oauthdeny",
                      "email":"oauth.deny@nexra.local",
                      "password":"Password@123"
                    }
                    """))
            .andExpect(status().isOk())
            .andReturn();

        String accessToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.data.accessToken");

        mockMvc.perform(get("/api/v1/oauth-clients")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isForbidden());
    }
}
