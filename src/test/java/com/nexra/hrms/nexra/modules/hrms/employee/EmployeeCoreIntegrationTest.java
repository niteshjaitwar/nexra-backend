package com.nexra.hrms.nexra.modules.hrms.employee;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeCoreIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void employeeCoreCrudFlowWorks() throws Exception {
        String token = bearerToken("ACME", List.of("ROLE_PLATFORM_ADMIN"));

        mockMvc.perform(put("/api/v1/employee-core/organization-profile")
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
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.tenantCode").value("ACME"));

        String departmentId = mockMvc.perform(post("/api/v1/employee-core/departments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "code":"ENG",
                      "name":"Engineering"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.code").value("ENG"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String extractedDepartmentId = JsonTestUtils.readJsonPath(departmentId, "$.data.departmentId");

        mockMvc.perform(post("/api/v1/employee-core/employees")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "employeeCode":"E001",
                      "firstName":"Nex",
                      "lastName":"User",
                      "workEmail":"nex.user@acme.test",
                      "departmentId":"%s",
                      "designation":"Software Engineer",
                      "status":"ACTIVE",
                      "joinDate":"%s",
                      "monthlyBasicSalary":50000
                    }
                    """.formatted(extractedDepartmentId, LocalDate.of(2026, 1, 1))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.employeeCode").value("E001"))
            .andExpect(jsonPath("$.data.fullName").value("Nex User"));

        mockMvc.perform(get("/api/v1/employee-core/summary")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.tenantCode").value("ACME"))
            .andExpect(jsonPath("$.data.organizationProfileConfigured").value(true))
            .andExpect(jsonPath("$.data.activeDepartments").value(1))
            .andExpect(jsonPath("$.data.activeEmployees").value(1));
    }

    @Test
    void rejectsTenantMismatch() throws Exception {
        String token = bearerToken("OTHER", List.of("ROLE_PLATFORM_ADMIN"));

        mockMvc.perform(get("/api/v1/employee-core/summary")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void rejectsUnauthenticatedProtectedRequests() throws Exception {
        mockMvc.perform(get("/api/v1/employee-core/summary")
                .param("tenantCode", "ACME"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Authentication is required."));
    }

    @Test
    void echoesCorrelationIdHeader() throws Exception {
        mockMvc.perform(get("/api/v1/employee-core/status")
                .header("X-Request-Id", "req-employee-core-001"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Request-Id", "req-employee-core-001"));
    }

    @Test
    void rejectsInvalidTenantCodeFormat() throws Exception {
        String token = bearerToken("ACME", List.of("ROLE_PLATFORM_ADMIN"));

        mockMvc.perform(put("/api/v1/employee-core/organization-profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME invalid",
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
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Validation failed."));
    }

    @Test
    void rejectsInvalidBearerToken() throws Exception {
        mockMvc.perform(get("/api/v1/employee-core/summary")
                .header("Authorization", "Bearer invalid-token")
                .param("tenantCode", "ACME"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Invalid or expired token."));
    }

    @Test
    void rejectsForbiddenAdministrationRequestsForNonHrRoles() throws Exception {
        String token = bearerToken("ACME", List.of("ROLE_EMPLOYEE"));

        mockMvc.perform(post("/api/v1/employee-core/departments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "code":"OPS",
                      "name":"Operations"
                    }
                    """))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("User does not have employee-core administration permission"));
    }

    @Test
    void rejectsDuplicateDepartmentCodeWithinTenant() throws Exception {
        String token = bearerToken("ACME", List.of("ROLE_HR_ADMIN"));

        mockMvc.perform(post("/api/v1/employee-core/departments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "code":"FIN",
                      "name":"Finance"
                    }
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/employee-core/departments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "code":"fin",
                      "name":"Finance Shared"
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Department code already exists for tenant: fin"));
    }

    @Test
    void rejectsEmployeeCreationWhenDepartmentDoesNotExist() throws Exception {
        String token = bearerToken("ACME", List.of("ROLE_HR_ADMIN"));

        mockMvc.perform(post("/api/v1/employee-core/employees")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "employeeCode":"E404",
                      "firstName":"Ghost",
                      "lastName":"User",
                      "workEmail":"ghost.user@acme.test",
                      "departmentId":"missing-department-id",
                      "designation":"Engineer",
                      "status":"ACTIVE",
                      "joinDate":"2026-01-01",
                      "monthlyBasicSalary":50000
                    }
                    """))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Department not found for tenant: missing-department-id"));
    }

    @Test
    void rejectsMalformedJsonPayloads() throws Exception {
        String token = bearerToken("ACME", List.of("ROLE_HR_ADMIN"));

        mockMvc.perform(post("/api/v1/employee-core/departments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"tenantCode":"ACME","code":"QA","name":"Quality"
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("MALFORMED_JSON"))
            .andExpect(jsonPath("$.message").value("Invalid request payload."));
    }

    private String bearerToken(final String tenantCode, final List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
            .subject("admin@acme.test")
            .claim("uid", UUID.randomUUID().toString())
            .claim("tenant", tenantCode)
            .claim("roles", roles)
            .claim("products", List.of("HRMS"))
            .signWith(key)
            .compact();
    }
}
