package com.nexra.hrms.nexra.modules.payroll;

import com.jayway.jsonpath.JsonPath;
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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end integration coverage for the payroll module inside the modular monolith.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PayrollIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void payrollReferenceFlowWorks() throws Exception {
        String token = bearerToken("ACME", List.of("ROLE_PAYROLL_ADMIN"));

        mockMvc.perform(put("/api/v1/payroll/organization-profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "organizationName":"Acme Corp",
                      "legalEntityName":"Acme Legal Pvt Ltd",
                      "addressLine1":"Line 1",
                      "city":"Pune",
                      "state":"Maharashtra",
                      "country":"India",
                      "postalCode":"411001",
                      "currency":"INR",
                      "defaultTaxPercent":10,
                      "defaultProvidentFundPercent":12,
                      "payrollContactEmail":"payroll@acme.test"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.tenantCode").value("ACME"));

        mockMvc.perform(post("/api/v1/payroll/employees")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "employeeId":"EMP-1",
                      "employeeCode":"E001",
                      "employeeName":"Nex User",
                      "department":"Engineering",
                      "designation":"Software Engineer",
                      "monthlyBasicSalary":50000,
                      "email":"nex.user@acme.test"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.employeeCode").value("E001"));

        String generatedPayroll = mockMvc.perform(post("/api/v1/payroll/generate/from-profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "employeeId":"EMP-1",
                      "payPeriod":"2026-03",
                      "allowances":[{"name":"HRA","amount":15000}],
                      "deductions":[{"name":"Professional Tax","amount":200}]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.slip.employeeCode").value("E001"))
            .andExpect(jsonPath("$.data.slip.authDependencyStatus.reachable").value(true))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String slipId = JsonPath.read(generatedPayroll, "$.data.slip.slipId");

        mockMvc.perform(get("/api/v1/payroll/{slipId}", slipId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.slipId").value(slipId));

        mockMvc.perform(get("/api/v1/payroll/payslips/{slipId}/pdf", slipId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andExpect(header().string("Content-Disposition", containsString("attachment;")));

        mockMvc.perform(get("/api/v1/payroll/dependencies/auth")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.reachable").value(true))
            .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    void rejectsTenantMismatch() throws Exception {
        String token = bearerToken("OTHER", List.of("ROLE_PAYROLL_ADMIN"));

        mockMvc.perform(get("/api/v1/payroll")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void rejectsUnauthenticatedProtectedRequests() throws Exception {
        mockMvc.perform(get("/api/v1/payroll/dependencies/auth"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Authentication is required."));
    }

    @Test
    void rejectsInvalidBearerToken() throws Exception {
        mockMvc.perform(get("/api/v1/payroll/dependencies/auth")
                .header("Authorization", "Bearer invalid-token"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid or expired token."));
    }

    @Test
    void rejectsForbiddenPayrollAdministrationRequests() throws Exception {
        String token = bearerToken("ACME", List.of("ROLE_EMPLOYEE"));

        mockMvc.perform(post("/api/v1/payroll/employees")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "employeeId":"EMP-2",
                      "employeeCode":"E002",
                      "employeeName":"Forbidden User",
                      "department":"Operations",
                      "designation":"Associate",
                      "monthlyBasicSalary":25000
                    }
                    """))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("User does not have payroll administration permission"));
    }

    @Test
    void exposesPublicBrandingEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/branding"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.companyName").value("Nexra HRMS Test"));
    }

    @Test
    void echoesCorrelationIdHeader() throws Exception {
        mockMvc.perform(get("/api/v1/payroll/status")
                .header("X-Request-Id", "req-payroll-001"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Request-Id", "req-payroll-001"));
    }

    private String bearerToken(final String tenantCode, final List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
            .subject("payroll.admin@acme.test")
            .claim("uid", UUID.randomUUID().toString())
            .claim("tenant", tenantCode)
            .claim("roles", roles)
            .signWith(key)
            .compact();
    }
}
