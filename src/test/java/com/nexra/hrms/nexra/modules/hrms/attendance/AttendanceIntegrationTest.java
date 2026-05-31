package com.nexra.hrms.nexra.modules.hrms.attendance;

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
 * Integration coverage for attendance workflows inside the modular monolith.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AttendanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void attendanceCheckInOutFlowWorks() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = bearerToken(userId, "ACME", List.of("ROLE_PLATFORM_ADMIN", "ROLE_HR_ADMIN"));

        mockMvc.perform(put("/api/v1/attendance/shifts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "code":"GEN",
                      "name":"General Shift",
                      "startTime":"09:00",
                      "endTime":"18:00",
                      "graceMinutes":15
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(header().exists("X-Request-Id"))
            .andExpect(jsonPath("$.data.code").value("GEN"));

        mockMvc.perform(post("/api/v1/attendance/check-in")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "employeeId":"%s",
                      "workDate":"2026-03-10",
                      "shiftCode":"GEN"
                    }
                    """.formatted(userId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("CHECKED_IN"));

        mockMvc.perform(post("/api/v1/attendance/check-out")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "employeeId":"%s",
                      "workDate":"2026-03-10"
                    }
                    """.formatted(userId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.checkOutAt").exists());

        mockMvc.perform(get("/api/v1/attendance/records")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME")
                .param("employeeId", userId.toString())
                .param("fromDate", "2026-03-01")
                .param("toDate", "2026-03-31"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[0].employeeId").value(userId.toString()))
            .andExpect(jsonPath("$.data.items[0].workDate").value("2026-03-10"));

        mockMvc.perform(get("/api/v1/attendance/summary")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME")
                .param("employeeId", userId.toString())
                .param("fromDate", "2026-03-01")
                .param("toDate", "2026-03-31"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.recordCount").value(1));
    }

    @Test
    void attendanceRegularizationWorkflowAppliesApprovedTimes() throws Exception {
        UUID employeeId = UUID.randomUUID();
        String employeeToken = bearerToken(employeeId, "ACME", List.of("ROLE_EMPLOYEE"));
        String adminToken = bearerToken(UUID.randomUUID(), "ACME", List.of("ROLE_HR_ADMIN"));

        mockMvc.perform(put("/api/v1/attendance/shifts")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "code":"GEN",
                      "name":"General Shift",
                      "startTime":"09:00",
                      "endTime":"18:00",
                      "graceMinutes":15
                    }
                    """))
            .andExpect(status().isOk());

        final String submitResponse = mockMvc.perform(post("/api/v1/attendance/regularizations")
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "employeeId":"%s",
                      "workDate":"2026-03-12",
                      "reason":"Missed punch",
                      "requestedCheckInAt":"2026-03-12T09:00:00Z",
                      "requestedCheckOutAt":"2026-03-12T18:00:00Z"
                    }
                    """.formatted(employeeId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.status").value("PENDING"))
            .andReturn().getResponse().getContentAsString();

        final String requestId = com.jayway.jsonpath.JsonPath.read(submitResponse, "$.data.id");

        mockMvc.perform(post("/api/v1/attendance/regularizations/{requestId}/approve", requestId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"tenantCode":"ACME","decisionComment":"Approved missed punch"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(get("/api/v1/attendance/records")
                .header("Authorization", "Bearer " + employeeToken)
                .param("tenantCode", "ACME")
                .param("employeeId", employeeId.toString())
                .param("fromDate", "2026-03-12")
                .param("toDate", "2026-03-12"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[0].checkInAt").exists())
            .andExpect(jsonPath("$.data.items[0].checkOutAt").exists());
    }

    @Test
    void attendanceTenantMismatchRejected() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = bearerToken(userId, "OTHER", List.of("ROLE_PLATFORM_ADMIN"));
        mockMvc.perform(get("/api/v1/attendance/shifts")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void rejectsMalformedJsonPayloads() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = bearerToken(userId, "ACME", List.of("ROLE_HR_ADMIN"));

        mockMvc.perform(post("/api/v1/attendance/check-in")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"tenantCode":"ACME","employeeId":"%s","workDate":"2026-03-10","shiftCode":"GEN"
                    """.formatted(userId)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("MALFORMED_JSON"))
            .andExpect(jsonPath("$.message").value("Invalid request payload."));
    }

    @Test
    void rejectsCrossEmployeeAccessForNonAdmin() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID otherEmployeeId = UUID.randomUUID();
        String token = bearerToken(userId, "ACME", List.of("ROLE_EMPLOYEE"));

        mockMvc.perform(get("/api/v1/attendance/records")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME")
                .param("employeeId", otherEmployeeId.toString())
                .param("fromDate", "2026-03-01")
                .param("toDate", "2026-03-31"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("User cannot access attendance for another employee"));
    }

    @Test
    void rejectsInvalidDateRangeForAttendanceQueries() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = bearerToken(userId, "ACME", List.of("ROLE_HR_ADMIN"));

        mockMvc.perform(get("/api/v1/attendance/summary")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME")
                .param("employeeId", userId.toString())
                .param("fromDate", "2026-03-31")
                .param("toDate", "2026-03-01"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("toDate must be on or after fromDate"));
    }

    private String bearerToken(final UUID userId, final String tenantCode, final List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
            .subject("attendance.admin@acme.test")
            .claim("uid", userId.toString())
            .claim("tenant", tenantCode)
            .claim("roles", roles)
            .claim("products", List.of("HRMS"))
            .signWith(key)
            .compact();
    }
}

