package com.nexra.hrms.nexra.modules.auth;

import com.nexra.hrms.nexra.modules.auth.repository.RefreshTokenRepository;
import com.nexra.hrms.nexra.modules.auth.repository.TenantRepository;
import com.nexra.hrms.nexra.modules.auth.repository.UserAccountRepository;
import com.nexra.hrms.nexra.modules.auth.repository.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Validates request payload rejection paths for public auth and tenant APIs.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Validation & malformed requests")
class AuthPublicApiValidationIntegrationTest {

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
    void clean() {
        verificationTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userAccountRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    @Test
    @DisplayName("Malformed JSON returns 400 with ApiResponse")
    void shouldRejectMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not-json"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Bean validation rejects invalid email on register")
    void shouldRejectInvalidEmailOnRegister() throws Exception {
        mockMvc.perform(post("/api/v1/tenants")
                .with(user("platform-admin").roles("PLATFORM_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"valtenant\",\"name\":\"T\",\"enterprise\":true}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"valtenant",
                      "email":"not-an-email",
                      "password":"Password@123",
                      "firstName":"X",
                      "lastName":"Y",
                      "accountType":"ENTERPRISE"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Invalid tenant code pattern on tenant create returns 400")
    void shouldRejectInvalidTenantCode() throws Exception {
        mockMvc.perform(post("/api/v1/tenants")
                .with(user("platform-admin").roles("PLATFORM_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"Invalid_Code\",\"name\":\"X\",\"enterprise\":true}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message", containsString("lowercase")));
    }
}
