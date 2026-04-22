package com.nexra.hrms.nexra.modules.hrms.leave;

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
 * Integration coverage for leave workflows inside the modular monolith.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LeaveManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void leaveRequestApprovalFlowWorks() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = bearerToken(userId, "ACME", List.of("ROLE_PLATFORM_ADMIN", "ROLE_HR_ADMIN"));

        mockMvc.perform(put("/api/v1/leave/leave-types")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "code":"CL",
                      "name":"Casual Leave",
                      "paid":true,
                      "defaultAnnualQuota":12
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(header().exists("X-Request-Id"))
            .andExpect(jsonPath("$.data.code").value("CL"));

        mockMvc.perform(put("/api/v1/leave/balances")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "employeeId":"%s",
                      "leaveTypeCode":"CL",
                      "openingBalance":2,
                      "accruedBalance":5,
                      "adjustedBalance":0
                    }
                    """.formatted(userId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.availableBalance").value(7.00));

        String requestJson = mockMvc.perform(post("/api/v1/leave/requests")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "employeeId":"%s",
                      "leaveTypeCode":"CL",
                      "startDate":"2026-03-01",
                      "endDate":"2026-03-02",
                      "reason":"Family event"
                    }
                    """.formatted(userId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("PENDING"))
            .andReturn().getResponse().getContentAsString();

        String requestId = OBJECT_MAPPER.readTree(requestJson).path("data").path("requestId").asText();

        mockMvc.perform(post("/api/v1/leave/requests/{requestId}/approve", requestId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "decisionComment":"Approved"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(get("/api/v1/leave/balances")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME")
                .param("employeeId", userId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].usedBalance").value(2.00))
            .andExpect(jsonPath("$.data[0].availableBalance").value(5.00));

        mockMvc.perform(get("/api/v1/leave/requests")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME")
                .param("employeeId", userId.toString())
                .param("status", "APPROVED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].requestId").value(requestId));
    }

    @Test
    void tenantMismatchRejected() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = bearerToken(userId, "OTHER", List.of("ROLE_PLATFORM_ADMIN"));
        mockMvc.perform(get("/api/v1/leave/leave-types")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false));
    }

    private String bearerToken(final UUID userId, final String tenantCode, final List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
            .subject("hr.admin@acme.test")
            .claim("uid", userId.toString())
            .claim("tenant", tenantCode)
            .claim("roles", roles)
            .signWith(key)
            .compact();
    }
}

