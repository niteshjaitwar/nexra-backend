package com.nexra.hrms.nexra.modules.payroll;

import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import java.util.Base64;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
                      "payrollContactEmail":"payroll@acme.test",
                      "brandingLogoPath":"/branding/nexra-banner.png",
                      "brandingCompanyName":"Acme Payroll",
                      "brandingWatermarkText":"ACME CONFIDENTIAL"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.tenantCode").value("ACME"))
            .andExpect(jsonPath("$.data.brandingCompanyName").value("Acme Payroll"));

        MockMultipartFile logoFile = new MockMultipartFile(
            "logoFile",
            "acme-logo.png",
            MediaType.IMAGE_PNG_VALUE,
            Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAASsJTYQAAAAASUVORK5CYII=")
        );
        MockMultipartHttpServletRequestBuilder logoUpload = multipart("/api/v1/payroll/organization-profile/logo")
            .file(logoFile)
            .param("tenantCode", "ACME");
        logoUpload.with(request -> {
            request.setMethod("POST");
            return request;
        });

        mockMvc.perform(logoUpload.header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.brandingLogoPath").value(containsString("/api/v1/branding/assets/ACME/logo-")));

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

        mockMvc.perform(get("/api/v1/payroll/payslips/{slipId}/html", slipId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Acme Payroll")))
            .andExpect(content().string(containsString("ACME CONFIDENTIAL")))
            .andExpect(content().string(containsString("data:image/png;base64,")));

        mockMvc.perform(get("/api/v1/payroll/dependencies/auth")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.reachable").value(true))
            .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    void generatesAndListsStatutoryFilingArtifacts() throws Exception {
        String token = bearerToken("FILING_CO", List.of("ROLE_PAYROLL_ADMIN"));

        mockMvc.perform(post("/api/v1/payroll/statutory/IN/filings")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "period":"2026-03",
                      "grossAmounts":[50000, 16000]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.filingType").value("PF_ESI_PT_RETURN"))
            .andExpect(jsonPath("$.data.status").value("GENERATED"))
            .andExpect(jsonPath("$.data.employeeCount").value(2))
            .andExpect(jsonPath("$.data.referenceNumber").value(containsString("PF_ESI_PT_RETURN-IN-202603")))
            .andExpect(jsonPath("$.data.componentTotals").isNotEmpty());

        // Duplicate filing for the same period/type is rejected.
        mockMvc.perform(post("/api/v1/payroll/statutory/IN/filings")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "period":"2026-03",
                      "grossAmounts":[50000]
                    }
                    """))
            .andExpect(status().isConflict());

        mockMvc.perform(get("/api/v1/payroll/statutory/IN/filings")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].period").value("2026-03"))
            .andExpect(jsonPath("$.data[0].filingType").value("PF_ESI_PT_RETURN"));

        final String generateResponse = mockMvc.perform(post("/api/v1/payroll/statutory/US/filings")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "period":"2026-04",
                      "grossAmounts":[9000]
                    }
                    """))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        final String filingId = com.jayway.jsonpath.JsonPath.read(generateResponse, "$.data.id");

        mockMvc.perform(get("/api/v1/payroll/statutory/US/filings/{id}", filingId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(filingId))
            .andExpect(jsonPath("$.data.status").value("GENERATED"));

        mockMvc.perform(post("/api/v1/payroll/statutory/US/filings/{id}/submit", filingId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"submissionReference":"941-202604"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("SUBMITTED"));

        mockMvc.perform(post("/api/v1/payroll/statutory/US/filings/{id}/lock", filingId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("LOCKED"));

        mockMvc.perform(get("/api/v1/payroll/statutory/US/filings/{id}/export", filingId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.submissionFormat").value("FORM_941_JSON"))
            .andExpect(jsonPath("$.data.filingId").value(filingId));

        mockMvc.perform(get("/api/v1/payroll/statutory/US/filings/{id}/export", filingId)
                .header("Authorization", "Bearer " + token)
                .param("format", "xml"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isString())
            .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.containsString("<StatutoryFilingExport")))
            .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.containsString("FORM_941_JSON")));
    }

    @Test
    void calculatesGermanyStatutoryBreakdown() throws Exception {
        String token = bearerToken("FILING_CO", List.of("ROLE_PAYROLL_ADMIN"));

        mockMvc.perform(get("/api/v1/payroll/statutory/DE/breakdown")
                .header("Authorization", "Bearer " + token)
                .param("grossMonthly", "8000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.countryCode").value("DE"))
            // Pension 9.3% of 7550 cap = 702.15 employee; health 7.3% of 5512.50 cap = 402.41 employee.
            .andExpect(jsonPath("$.data.employeeTotal").value(1104.56))
            .andExpect(jsonPath("$.data.employerTotal").value(1104.56));
    }

    @Test
    void rejectsUnauthenticatedFilingGeneration() throws Exception {
        mockMvc.perform(post("/api/v1/payroll/statutory/IN/filings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"period\":\"2026-04\",\"grossAmounts\":[10000]}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsFilingGenerationForWrongProductScope() throws Exception {
        String token = bearerToken("FILING_CO", List.of("ROLE_PAYROLL_ADMIN"), Set.of("CRM"));
        mockMvc.perform(post("/api/v1/payroll/statutory/IN/filings")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"period\":\"2026-04\",\"grossAmounts\":[10000]}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("User does not have payroll product access"));
    }

    @Test
    void rejectsFilingGenerationForNonAdminRole() throws Exception {
        String token = bearerToken("FILING_CO", List.of("ROLE_EMPLOYEE"));
        mockMvc.perform(post("/api/v1/payroll/statutory/IN/filings")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"period\":\"2026-04\",\"grossAmounts\":[10000]}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("User does not have payroll administration permission"));
    }

    @Test
    void calculatesUnitedStatesFicaBreakdown() throws Exception {
        String token = bearerToken("FILING_CO", List.of("ROLE_PAYROLL_ADMIN"));

        mockMvc.perform(get("/api/v1/payroll/statutory/US/breakdown")
                .header("Authorization", "Bearer " + token)
                .param("grossMonthly", "10000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.countryCode").value("US"))
            // Social Security 6.2% of 10000 = 620 (employee) + 620 (employer);
            // Medicare 1.45% of 10000 = 145 each. Employee total = 765.
            .andExpect(jsonPath("$.data.employeeTotal").value(765.00))
            .andExpect(jsonPath("$.data.employerTotal").value(765.00));
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
    void exposesTenantBrandingEndpoint() throws Exception {
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
                      "payrollContactEmail":"payroll@acme.test",
                      "brandingCompanyName":"Acme Payroll",
                      "brandingWatermarkText":"ACME CONFIDENTIAL"
                    }
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/branding/ACME"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.companyName").value("Acme Payroll"))
            .andExpect(jsonPath("$.data.watermarkText").value("ACME CONFIDENTIAL"));
    }

    @Test
    void rejectsLogoUploadForTenantMismatch() throws Exception {
        String token = bearerToken("OTHER", List.of("ROLE_PAYROLL_ADMIN"));
        MockMultipartFile logoFile = new MockMultipartFile(
            "logoFile",
            "acme-logo.png",
            MediaType.IMAGE_PNG_VALUE,
            Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAASsJTYQAAAAASUVORK5CYII=")
        );
        MockMultipartHttpServletRequestBuilder request = multipart("/api/v1/payroll/organization-profile/logo")
            .file(logoFile)
            .param("tenantCode", "ACME");
        request.with(r -> {
            r.setMethod("POST");
            return r;
        });

        mockMvc.perform(request.header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("Tenant mismatch for payroll action"));
    }

    @Test
    void rejectsLogoUploadForNonAdminRole() throws Exception {
        String token = bearerToken("ACME", List.of("ROLE_EMPLOYEE"));
        MockMultipartFile logoFile = new MockMultipartFile(
            "logoFile",
            "acme-logo.png",
            MediaType.IMAGE_PNG_VALUE,
            Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAASsJTYQAAAAASUVORK5CYII=")
        );
        MockMultipartHttpServletRequestBuilder request = multipart("/api/v1/payroll/organization-profile/logo")
            .file(logoFile)
            .param("tenantCode", "ACME");
        request.with(r -> {
            r.setMethod("POST");
            return r;
        });

        mockMvc.perform(request.header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("User does not have payroll administration permission"));
    }

    @Test
    void rejectsInvalidLogoPayload() throws Exception {
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
                      "defaultProvidentFundPercent":12
                    }
                    """))
            .andExpect(status().isOk());

        MockMultipartFile badFile = new MockMultipartFile(
            "logoFile",
            "acme-logo.png",
            MediaType.IMAGE_PNG_VALUE,
            "not-an-image".getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartHttpServletRequestBuilder request = multipart("/api/v1/payroll/organization-profile/logo")
            .file(badFile)
            .param("tenantCode", "ACME");
        request.with(r -> {
            r.setMethod("POST");
            return r;
        });

        mockMvc.perform(request.header("Authorization", "Bearer " + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Logo file content is not a valid image."));
    }

    @Test
    void rejectsLogoUploadWhenOrganizationProfileMissing() throws Exception {
        String tenantCode = "MISSING_TENANT";
        String token = bearerToken(tenantCode, List.of("ROLE_PAYROLL_ADMIN"));
        MockMultipartFile logoFile = new MockMultipartFile(
            "logoFile",
            "acme-logo.png",
            MediaType.IMAGE_PNG_VALUE,
            Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAASsJTYQAAAAASUVORK5CYII=")
        );
        MockMultipartHttpServletRequestBuilder request = multipart("/api/v1/payroll/organization-profile/logo")
            .file(logoFile)
            .param("tenantCode", tenantCode);
        request.with(r -> {
            r.setMethod("POST");
            return r;
        });

        mockMvc.perform(request.header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Organization profile not found for tenant: " + tenantCode));
    }

    @Test
    void rejectsUnsupportedLogoFileType() throws Exception {
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
                      "defaultProvidentFundPercent":12
                    }
                    """))
            .andExpect(status().isOk());

        MockMultipartFile svgFile = new MockMultipartFile(
            "logoFile",
            "acme-logo.svg",
            "image/svg+xml",
            "<svg xmlns=\"http://www.w3.org/2000/svg\"></svg>".getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartHttpServletRequestBuilder request = multipart("/api/v1/payroll/organization-profile/logo")
            .file(svgFile)
            .param("tenantCode", "ACME");
        request.with(r -> {
            r.setMethod("POST");
            return r;
        });

        mockMvc.perform(request.header("Authorization", "Bearer " + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Logo file type is not supported."));
    }

    @Test
    void echoesCorrelationIdHeader() throws Exception {
        mockMvc.perform(get("/api/v1/payroll/status")
                .header("X-Request-Id", "req-payroll-001"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Request-Id", "req-payroll-001"));
    }

    @Test
    void rejectsMissingPayrollProductScope() throws Exception {
        String token = bearerToken("ACME", List.of("ROLE_PAYROLL_ADMIN"), null);
        mockMvc.perform(get("/api/v1/payroll/dependencies/auth")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("User does not have payroll product access"));
    }

    @Test
    void rejectsWrongProductScope() throws Exception {
        String token = bearerToken("ACME", List.of("ROLE_PAYROLL_ADMIN"), Set.of("CRM"));
        mockMvc.perform(get("/api/v1/payroll/dependencies/auth")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("User does not have payroll product access"));
    }

    private String bearerToken(final String tenantCode, final List<String> roles) {
        return bearerToken(tenantCode, roles, Set.of("PAYROLL"));
    }

    private String bearerToken(final String tenantCode, final List<String> roles, final Set<String> products) {
        SecretKey key = Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8));
        var builder = Jwts.builder()
            .subject("payroll.admin@acme.test")
            .claim("uid", UUID.randomUUID().toString())
            .claim("tenant", tenantCode)
            .claim("roles", roles);
        if (products != null) {
            builder.claim("products", products);
        }
        return builder.signWith(key).compact();
    }
}
