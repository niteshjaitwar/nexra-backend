package com.nexra.hrms.nexra.modules.crm;

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

@SpringBootTest(properties = "nexra.crm.enforce-auth=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CrmCaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void caseStateMachineAndAssignmentWorks() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_USER"), Set.of("CRM"));

        final String createResponse = mockMvc.perform(post("/api/v1/crm/cases")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "subject":"Login fails intermittently",
                      "description":"Customer cannot log in",
                      "ownerUserId":"u-100"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.status").value("OPEN"))
            .andExpect(jsonPath("$.data.priority").value("MEDIUM"))
            .andReturn().getResponse().getContentAsString();
        final String caseId = readId(createResponse);

        mockMvc.perform(post("/api/v1/crm/cases/{id}/status", caseId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetStatus\":\"IN_PROGRESS\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        mockMvc.perform(post("/api/v1/crm/cases/{id}/status", caseId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetStatus\":\"CLOSED\"}"))
            .andExpect(status().is(422))
            .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(post("/api/v1/crm/cases/{id}/status", caseId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetStatus\":\"NOT_A_STATUS\"}"))
            .andExpect(status().is(422));

        mockMvc.perform(post("/api/v1/crm/cases/{id}/status", caseId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetStatus\":\"RESOLVED\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("RESOLVED"));

        mockMvc.perform(post("/api/v1/crm/cases/{id}/status", caseId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetStatus\":\"CLOSED\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("CLOSED"));

        mockMvc.perform(post("/api/v1/crm/cases/{id}/assign", caseId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ownerUserId\":\"u-200\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.ownerUserId").value("u-200"));

        mockMvc.perform(get("/api/v1/crm/cases/{id}", caseId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }

    @Test
    void rejectsMissingCrmProductScope() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_USER"), Set.of("HRMS"));

        mockMvc.perform(post("/api/v1/crm/cases")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "subject":"Should be denied",
                      "ownerUserId":"u-1"
                    }
                    """))
            .andExpect(status().isForbidden());
    }

    @Test
    void enforcesTenantIsolationOnCaseRead() throws Exception {
        final String acmeToken = bearerToken("ACME", List.of("ROLE_USER"), Set.of("CRM"));
        final String betaToken = bearerToken("BETA", List.of("ROLE_USER"), Set.of("CRM"));

        final String createResponse = mockMvc.perform(post("/api/v1/crm/cases")
                .header("Authorization", "Bearer " + acmeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "subject":"Tenant scoped case",
                      "ownerUserId":"u-1"
                    }
                    """))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        final String caseId = readId(createResponse);

        mockMvc.perform(get("/api/v1/crm/cases/{id}", caseId)
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
            .subject("crm-user@nexra.test")
            .claim("uid", UUID.randomUUID().toString())
            .claim("tenant", tenantCode)
            .claim("roles", roles)
            .claim("products", products)
            .signWith(key)
            .compact();
    }
}
