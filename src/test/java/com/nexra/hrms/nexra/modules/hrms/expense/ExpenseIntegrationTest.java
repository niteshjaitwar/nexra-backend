package com.nexra.hrms.nexra.modules.hrms.expense;

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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExpenseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void expenseClaimApprovalAndReimbursementFlowWorks() throws Exception {
        UUID employeeId = UUID.randomUUID();
        String token = bearerToken(employeeId, "ACME", List.of("ROLE_PLATFORM_ADMIN", "ROLE_FINANCE_ADMIN"));

        mockMvc.perform(put("/api/v1/expense/categories")
                .header("X-Request-Id", "req-expense-001")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "code":"TRAVEL",
                      "name":"Travel",
                      "maxAmountPerClaim":5000,
                      "requiresReceipt":true,
                      "active":true
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Request-Id", "req-expense-001"))
            .andExpect(jsonPath("$.data.code").value("TRAVEL"));

        String claimResp = mockMvc.perform(post("/api/v1/expense/claims")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tenantCode":"ACME",
                      "employeeId":"%s",
                      "claimDate":"2026-03-15",
                      "title":"Client Visit Expense",
                      "currency":"INR",
                      "items":[
                        {
                          "expenseDate":"2026-03-14",
                          "categoryCode":"TRAVEL",
                          "description":"Taxi to client office",
                          "amount":1200,
                          "receiptReference":"RCPT-001"
                        }
                      ]
                    }
                    """.formatted(employeeId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("SUBMITTED"))
            .andExpect(jsonPath("$.data.totalAmount").value(1200.00))
            .andReturn().getResponse().getContentAsString();

        String claimId = MAPPER.readTree(claimResp).path("data").path("claimId").asText();

        mockMvc.perform(post("/api/v1/expense/claims/{id}/approve", claimId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"tenantCode":"ACME","comment":"Approved by finance"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(post("/api/v1/expense/claims/{id}/reimburse", claimId)
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("REIMBURSED"))
            .andExpect(jsonPath("$.data.reimbursedAt").exists());

        mockMvc.perform(get("/api/v1/expense/claims")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME")
                .param("status", "REIMBURSED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].claimId").value(claimId));
    }

    @Test
    void tenantMismatchRejected() throws Exception {
        String token = bearerToken(UUID.randomUUID(), "OTHER", List.of("ROLE_PLATFORM_ADMIN"));
        mockMvc.perform(get("/api/v1/expense/categories")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void rejectsUnauthenticatedProtectedRequests() throws Exception {
        mockMvc.perform(get("/api/v1/expense/claims")
                .param("tenantCode", "ACME"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Authentication is required."));
    }

    @Test
    void rejectsForbiddenCrossEmployeeClaimLookup() throws Exception {
        String token = bearerToken(UUID.randomUUID(), "ACME", List.of("ROLE_EMPLOYEE"));
        mockMvc.perform(get("/api/v1/expense/claims")
                .header("Authorization", "Bearer " + token)
                .param("tenantCode", "ACME")
                .param("employeeId", UUID.randomUUID().toString()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("User cannot access expense claims for another employee"));
    }

    private String bearerToken(final UUID userId, final String tenantCode, final List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
            .subject("finance@acme.test")
            .claim("uid", userId.toString())
            .claim("tenant", tenantCode)
            .claim("roles", roles)
            .signWith(key)
            .compact();
    }
}

