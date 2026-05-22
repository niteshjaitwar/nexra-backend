package com.nexra.hrms.nexra.modules.crm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "nexra.crm.enforce-auth=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CrmAccountDealIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void accountAndDealCrudFlowWorks() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_CRM_ADMIN"), Set.of("CRM"));

        final String createAccountResponse = mockMvc.perform(post("/api/v1/crm/accounts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":"Acme Enterprise",
                      "website":"https://acme.test",
                      "industry":"Manufacturing",
                      "ownerUserId":"u-1001"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("Acme Enterprise"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        final String accountId = readId(createAccountResponse);

        mockMvc.perform(get("/api/v1/crm/accounts")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalItems").value(1));

        mockMvc.perform(put("/api/v1/crm/accounts/{accountId}", accountId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "industry":"SaaS"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.industry").value("SaaS"));

        final String createDealResponse = mockMvc.perform(post("/api/v1/crm/deals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "accountId":"%s",
                      "title":"Acme Platform Rollout",
                      "stage":"QUALIFICATION",
                      "valueAmount":450000,
                      "currency":"INR",
                      "ownerUserId":"u-1001"
                    }
                    """.formatted(accountId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.title").value("Acme Platform Rollout"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        final String dealId = readId(createDealResponse);

        mockMvc.perform(put("/api/v1/crm/deals/{dealId}", dealId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "stage":"WON"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.stage").value("WON"));

        mockMvc.perform(get("/api/v1/crm/modules/{moduleKey}/pipeline", "crm-deals")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalDeals").value(1))
            .andExpect(jsonPath("$.data.wonDeals").value(1));

        mockMvc.perform(delete("/api/v1/crm/deals/{dealId}", dealId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(delete("/api/v1/crm/accounts/{accountId}", accountId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    private String bearerToken(final String tenantCode, final List<String> roles, final Set<String> products) {
        final SecretKey key = Keys.hmacShaKeyFor("test-jwt-secret-test-jwt-secret-test-jwt".getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
            .subject("crm-admin@nexra.test")
            .claim("uid", UUID.randomUUID().toString())
            .claim("tenant", tenantCode)
            .claim("roles", roles)
            .claim("products", products)
            .signWith(key)
            .compact();
    }

    private String readId(final String payload) throws Exception {
        final JsonNode root = objectMapper.readTree(payload);
        return root.path("data").path("id").asText();
    }
}

