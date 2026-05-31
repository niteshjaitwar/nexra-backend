package com.nexra.hrms.nexra.common.workflow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkflowInstanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void listsInstanceAndStepHistoryAfterOpsApprovalWorkflow() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_USER"), Set.of("OPS"));

        final String approvalResponse = mockMvc.perform(post("/api/v1/operations/approvals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "referenceType":"EXPENSE",
                      "referenceId":"exp-100",
                      "requestedByUserId":"u-1",
                      "notes":"Need approval"
                    }
                    """))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(post("/api/v1/operations/approvals/{id}/decision", readId(approvalResponse))
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "decision":"APPROVE",
                      "approverUserId":"u-2"
                    }
                    """))
            .andExpect(status().isOk());

        final String listResponse = mockMvc.perform(get("/api/v1/workflow/instances")
                .header("Authorization", "Bearer " + token)
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[0].moduleKey").value("operations-approvals"))
            .andExpect(jsonPath("$.data.items[0].status").value("COMPLETED"))
            .andReturn().getResponse().getContentAsString();

        final String instanceId = objectMapper.readTree(listResponse).path("data").path("items").get(0).path("id").asText();

        mockMvc.perform(get("/api/v1/workflow/instances/{id}/history", instanceId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].action").value("STARTED"))
            .andExpect(jsonPath("$.data[?(@.action == 'COMPLETED')]").exists());
    }

    @Test
    void advancesWorkflowInstanceViaHttpApi() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_USER"), Set.of("OPS"));

        final String approvalResponse = mockMvc.perform(post("/api/v1/operations/approvals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "referenceType":"EXPENSE",
                      "referenceId":"exp-advance",
                      "requestedByUserId":"u-1"
                    }
                    """))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        final String listResponse = mockMvc.perform(get("/api/v1/workflow/instances")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        final String instanceId = objectMapper.readTree(listResponse).path("data").path("items").get(0).path("id").asText();

        mockMvc.perform(post("/api/v1/workflow/instances/{id}/advance", instanceId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"approve":true,"notes":"Approved via workflow API"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    void rejectsWorkflowReadWithoutProductScope() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_USER"), Set.of("UNKNOWN_PRODUCT"));
        mockMvc.perform(get("/api/v1/workflow/instances")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void enforcesTenantIsolationOnWorkflowInstanceRead() throws Exception {
        final String acmeToken = bearerToken("ACME", List.of("ROLE_USER"), Set.of("OPS"));
        final String betaToken = bearerToken("BETA", List.of("ROLE_USER"), Set.of("OPS"));

        mockMvc.perform(post("/api/v1/operations/approvals")
                .header("Authorization", "Bearer " + acmeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "referenceType":"EXPENSE",
                      "referenceId":"exp-200",
                      "requestedByUserId":"u-1"
                    }
                    """))
            .andExpect(status().isCreated());

        final String listResponse = mockMvc.perform(get("/api/v1/workflow/instances")
                .header("Authorization", "Bearer " + acmeToken))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        final String instanceId = objectMapper.readTree(listResponse).path("data").path("items").get(0).path("id").asText();

        mockMvc.perform(get("/api/v1/workflow/instances/{id}", instanceId)
                .header("Authorization", "Bearer " + betaToken))
            .andExpect(status().isNotFound());
    }

    private String readId(final String responseBody) throws Exception {
        final JsonNode root = objectMapper.readTree(responseBody);
        return root.path("data").path("id").asText();
    }

    private String bearerToken(final String tenantCode, final List<String> roles, final Set<String> products) {
        final SecretKey key = Keys.hmacShaKeyFor("test-jwt-secret-test-jwt-secret-test-jwt".getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
            .subject("ops-user@nexra.test")
            .claim("uid", UUID.randomUUID().toString())
            .claim("tenant", tenantCode)
            .claim("roles", roles)
            .claim("products", products)
            .signWith(key)
            .compact();
    }
}
