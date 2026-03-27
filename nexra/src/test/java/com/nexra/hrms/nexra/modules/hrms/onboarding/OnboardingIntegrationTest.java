package com.nexra.hrms.nexra.modules.hrms.onboarding;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration coverage for onboarding workflows inside the modular monolith.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OnboardingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void onboardingFlowWorks() throws Exception {
        String token = bearerToken(UUID.randomUUID(), "ACME", List.of("ROLE_PLATFORM_ADMIN"));

        String planResponse = mockMvc.perform(post("/api/v1/onboarding/plans")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "employeeId":"E1",
                      "planName":"New Joiner Plan"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(header().exists("X-Request-Id"))
            .andReturn().getResponse().getContentAsString();

        String planId = OBJECT_MAPPER.readTree(planResponse).path("data").path("planId").asText();

        String taskResponse = mockMvc.perform(post("/api/v1/onboarding/plans/{planId}/tasks", planId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "taskName":"Laptop Allocation",
                      "ownerTeam":"IT"
                    }
                    """))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String taskId = OBJECT_MAPPER.readTree(taskResponse).path("data").path("taskId").asText();

        mockMvc.perform(post("/api/v1/onboarding/tasks/{taskId}/complete", taskId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        mockMvc.perform(get("/api/v1/onboarding/summary")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalPlans").value(1))
            .andExpect(jsonPath("$.data.completedTasks").value(1));
    }

    @Test
    void tenantMismatchRejected() throws Exception {
        String token = bearerToken(UUID.randomUUID(), "OTHER", List.of("ROLE_PLATFORM_ADMIN"));
        mockMvc.perform(get("/api/v1/onboarding/plans")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false));
    }

    private String bearerToken(final UUID userId, final String tenantCode, final List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
            .subject("onboarding.admin@acme.test")
            .claim("uid", userId.toString())
            .claim("tenant", tenantCode)
            .claim("roles", roles)
            .signWith(key)
            .compact();
    }
}
