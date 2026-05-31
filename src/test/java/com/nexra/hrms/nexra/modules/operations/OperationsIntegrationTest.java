package com.nexra.hrms.nexra.modules.operations;

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
class OperationsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void projectTaskAndApprovalLifecycleWorks() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_USER"), Set.of("OPS"));

        final String projectResponse = mockMvc.perform(post("/api/v1/operations/projects")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code":"PRJ-100",
                      "name":"Implementation Project",
                      "ownerUserId":"u-9001"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.status").value("ACTIVE"))
            .andReturn().getResponse().getContentAsString();
        final String projectId = readId(projectResponse);

        mockMvc.perform(post("/api/v1/operations/tasks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "projectId":"%s",
                      "title":"Kickoff",
                      "priority":"high"
                    }
                    """.formatted(projectId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.status").value("OPEN"))
            .andExpect(jsonPath("$.data.priority").value("HIGH"));

        mockMvc.perform(get("/api/v1/operations/tasks")
                .header("Authorization", "Bearer " + token)
                .param("projectId", projectId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalItems").value(1));

        final String approvalResponse = mockMvc.perform(post("/api/v1/operations/approvals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "referenceType":"PROJECT",
                      "referenceId":"%s",
                      "requestedByUserId":"u-9001"
                    }
                    """.formatted(projectId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.status").value("PENDING"))
            .andReturn().getResponse().getContentAsString();
        final String approvalId = readId(approvalResponse);

        mockMvc.perform(post("/api/v1/operations/approvals/{id}/decision", approvalId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "decision":"APPROVE",
                      "approverUserId":"u-7777"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(post("/api/v1/operations/approvals/{id}/decision", approvalId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "decision":"REJECT",
                      "approverUserId":"u-7777"
                    }
                    """))
            .andExpect(status().is(422))
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void rejectsMissingOperationsProductScope() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_USER"), Set.of("CRM"));

        mockMvc.perform(post("/api/v1/operations/projects")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code":"PRJ-DENY",
                      "name":"Should Be Denied",
                      "ownerUserId":"u-1"
                    }
                    """))
            .andExpect(status().isForbidden());
    }

    @Test
    void enforcesTenantIsolationOnApprovalDecision() throws Exception {
        final String acmeToken = bearerToken("ACME", List.of("ROLE_USER"), Set.of("OPS"));
        final String betaToken = bearerToken("BETA", List.of("ROLE_USER"), Set.of("OPS"));

        final String projectResponse = mockMvc.perform(post("/api/v1/operations/projects")
                .header("Authorization", "Bearer " + acmeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code":"PRJ-ISO",
                      "name":"Isolated Project",
                      "ownerUserId":"u-1"
                    }
                    """))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        final String projectId = readId(projectResponse);

        final String approvalResponse = mockMvc.perform(post("/api/v1/operations/approvals")
                .header("Authorization", "Bearer " + acmeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "referenceType":"PROJECT",
                      "referenceId":"%s",
                      "requestedByUserId":"u-1"
                    }
                    """.formatted(projectId)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        final String approvalId = readId(approvalResponse);

        mockMvc.perform(post("/api/v1/operations/approvals/{id}/decision", approvalId)
                .header("Authorization", "Bearer " + betaToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "decision":"APPROVE",
                      "approverUserId":"u-2"
                    }
                    """))
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
