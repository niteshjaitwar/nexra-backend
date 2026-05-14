package com.nexra.hrms.nexra.modules.hrms.performance;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration coverage for performance workflows inside the modular monolith.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PerformanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void performanceFlowWorks() throws Exception {
        String token = bearerToken(UUID.randomUUID(), "ACME", List.of("ROLE_PLATFORM_ADMIN"));

        String goalResponse = mockMvc.perform(put("/api/v1/performance/goals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "employeeId":"E1",
                      "title":"Improve CSAT",
                      "description":"Reach 95 percent satisfaction",
                      "targetDate":"2026-12-31",
                      "status":"ACTIVE"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(header().exists("X-Request-Id"))
            .andExpect(jsonPath("$.data.status").value("ACTIVE"))
            .andReturn().getResponse().getContentAsString();

        String goalId = OBJECT_MAPPER.readTree(goalResponse).path("data").path("goalId").asText();

        String reviewResponse = mockMvc.perform(post("/api/v1/performance/reviews")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "employeeId":"E1",
                      "reviewCycle":"FY26-Q4",
                      "employeeComments":"Completed major initiatives"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
            .andReturn().getResponse().getContentAsString();

        String reviewId = OBJECT_MAPPER.readTree(reviewResponse).path("data").path("reviewId").asText();

        mockMvc.perform(post("/api/v1/performance/reviews/{reviewId}/complete", reviewId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "managerScore":4.75,
                      "managerComments":"Strong delivery"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("COMPLETED"))
            .andExpect(jsonPath("$.data.managerScore").value(4.75));

        mockMvc.perform(get("/api/v1/performance/goals")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME")
                .param("employeeId", "E1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[0].goalId").value(goalId));

        mockMvc.perform(get("/api/v1/performance/summary")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalGoals").value(1))
            .andExpect(jsonPath("$.data.completedReviews").value(1));
    }

    @Test
    void tenantMismatchRejected() throws Exception {
        String token = bearerToken(UUID.randomUUID(), "OTHER", List.of("ROLE_PLATFORM_ADMIN"));

        mockMvc.perform(get("/api/v1/performance/goals")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false));
    }

    private String bearerToken(final UUID userId, final String tenantCode, final List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
            .subject("performance.admin@acme.test")
            .claim("uid", userId.toString())
            .claim("tenant", tenantCode)
            .claim("roles", roles)
            .signWith(key)
            .compact();
    }
}
