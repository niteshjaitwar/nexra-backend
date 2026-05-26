package com.nexra.hrms.nexra.modules.hrms.timesheet;

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
 * Integration coverage for timesheet workflows inside the modular monolith.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TimesheetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void timesheetEntryApprovalFlowWorks() throws Exception {
        UUID employeeId = UUID.randomUUID();
        String token = bearerToken(employeeId, "ACME", List.of("ROLE_PLATFORM_ADMIN", "ROLE_MANAGER"));

        mockMvc.perform(put("/api/v1/timesheet/projects")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "projectCode":"PRJ1",
                      "projectName":"CRM Rollout",
                      "clientName":"Acme Client",
                      "billable":true,
                      "active":true
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(header().exists("X-Request-Id"))
            .andExpect(jsonPath("$.data.projectCode").value("PRJ1"));

        String entryResp = mockMvc.perform(post("/api/v1/timesheet/entries")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "employeeId":"%s",
                      "workDate":"2026-03-18",
                      "projectCode":"PRJ1",
                      "taskName":"Implementation",
                      "hours":7.5,
                      "billable":true
                    }
                    """.formatted(employeeId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("SUBMITTED"))
            .andReturn().getResponse().getContentAsString();
        String entryId = MAPPER.readTree(entryResp).path("data").path("entryId").asText();

        mockMvc.perform(post("/api/v1/timesheet/entries/{id}/approve", entryId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"tenantCode":"ACME","comment":"Looks good"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(get("/api/v1/timesheet/summary")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME")
                .param("employeeId", employeeId.toString())
                .param("fromDate", "2026-03-01")
                .param("toDate", "2026-03-31"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.entryCount").value(1))
            .andExpect(jsonPath("$.data.totalHours").value(7.50))
            .andExpect(jsonPath("$.data.billableHours").value(7.50));
    }

    @Test
    void tenantMismatchRejected() throws Exception {
        String token = bearerToken(UUID.randomUUID(), "OTHER", List.of("ROLE_PLATFORM_ADMIN"));
        mockMvc.perform(get("/api/v1/timesheet/projects")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void rejectsMalformedJsonPayloads() throws Exception {
        UUID employeeId = UUID.randomUUID();
        String token = bearerToken(employeeId, "ACME", List.of("ROLE_MANAGER"));

        mockMvc.perform(post("/api/v1/timesheet/entries")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"tenantCode":"ACME","employeeId":"%s","workDate":"2026-03-18","projectCode":"PRJ1"
                    """.formatted(employeeId)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("MALFORMED_JSON"))
            .andExpect(jsonPath("$.message").value("Invalid request payload."));
    }

    @Test
    void rejectsProjectAdministrationForNonAdminRole() throws Exception {
        UUID employeeId = UUID.randomUUID();
        String token = bearerToken(employeeId, "ACME", List.of("ROLE_EMPLOYEE"));

        mockMvc.perform(put("/api/v1/timesheet/projects")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "projectCode":"PRJX",
                      "projectName":"Restricted",
                      "clientName":"Client",
                      "billable":true,
                      "active":true
                    }
                    """))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("User does not have timesheet administration permission"));
    }

    @Test
    void rejectsCrossEmployeeTimesheetAccessForNonAdmin() throws Exception {
        UUID employeeId = UUID.randomUUID();
        UUID anotherEmployeeId = UUID.randomUUID();
        String token = bearerToken(employeeId, "ACME", List.of("ROLE_EMPLOYEE"));

        mockMvc.perform(get("/api/v1/timesheet/entries")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME")
                .param("employeeId", anotherEmployeeId.toString())
                .param("fromDate", "2026-03-01")
                .param("toDate", "2026-03-31"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("User cannot access another employee timesheet"));
    }

    private String bearerToken(final UUID userId, final String tenantCode, final List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
            .subject("manager@acme.test")
            .claim("uid", userId.toString())
            .claim("tenant", tenantCode)
            .claim("roles", roles)
            .claim("products", List.of("HRMS"))
            .signWith(key)
            .compact();
    }
}

