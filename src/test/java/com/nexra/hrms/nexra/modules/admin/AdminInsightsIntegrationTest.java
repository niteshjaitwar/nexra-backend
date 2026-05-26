package com.nexra.hrms.nexra.modules.admin;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminInsightsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void adminInsightsEndpointsRequireAdminRoleAndReturnData() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_TENANT_ADMIN"), Set.of("HRMS", "CRM", "PAYROLL"));

        mockMvc.perform(get("/api/v1/admin/insights/summary")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.employees").exists())
            .andExpect(jsonPath("$.data.crmLeads").exists());

        mockMvc.perform(get("/api/v1/admin/insights/audit-events")
                .header("Authorization", "Bearer " + token)
                .param("limit", "10")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void adminInsightsRejectsNonAdminRole() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_EMPLOYEE"), Set.of("HRMS"));
        mockMvc.perform(get("/api/v1/admin/insights/summary")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void tenantAdminSummaryIsProductScoped() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_TENANT_ADMIN"), Set.of("HRMS"));

        mockMvc.perform(get("/api/v1/admin/insights/summary")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.employees").exists())
            .andExpect(jsonPath("$.data.expenseClaims").exists())
            .andExpect(jsonPath("$.data.crmLeads").doesNotExist())
            .andExpect(jsonPath("$.data.payrollSlips").doesNotExist());
    }

    @Test
    void tenantAdminCannotQueryAuditEventsForUnlicensedProductModule() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_TENANT_ADMIN"), Set.of("HRMS"));

        mockMvc.perform(get("/api/v1/admin/insights/audit-events")
                .header("Authorization", "Bearer " + token)
                .param("module", "CRM")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("Admin user does not have access to requested module insights."));
    }

    @Test
    void auditEventsRejectInvalidLimit() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_TENANT_ADMIN"), Set.of("HRMS"));

        mockMvc.perform(get("/api/v1/admin/insights/audit-events")
                .header("Authorization", "Bearer " + token)
                .param("limit", "201")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    private String bearerToken(final String tenantCode, final List<String> roles, final Set<String> products) {
        final SecretKey key = Keys.hmacShaKeyFor("test-jwt-secret-test-jwt-secret-test-jwt".getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
            .subject("admin@nexra.test")
            .claim("uid", UUID.randomUUID().toString())
            .claim("tenant", tenantCode)
            .claim("roles", roles)
            .claim("products", products)
            .signWith(key)
            .compact();
    }
}
