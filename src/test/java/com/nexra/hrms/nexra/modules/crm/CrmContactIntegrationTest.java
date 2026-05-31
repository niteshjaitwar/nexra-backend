package com.nexra.hrms.nexra.modules.crm;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "nexra.crm.enforce-auth=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CrmContactIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void contactCrudWithCustomFieldWorks() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_USER"), Set.of("CRM"), java.util.Map.of("CRM", "SALES_MANAGER"));

        mockMvc.perform(post("/api/v1/crm/admin/custom-fields")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "moduleKey":"crm-contacts",
                      "fieldKey":"tier",
                      "label":"Tier",
                      "fieldType":"TEXT",
                      "required":false,
                      "searchable":true,
                      "active":true
                    }
                    """))
            .andExpect(status().isCreated());

        final String accountResponse = mockMvc.perform(post("/api/v1/crm/accounts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":"Contact Account",
                      "ownerUserId":"u-1001"
                    }
                    """))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        final String accountId = readId(accountResponse);

        final String createContactResponse = mockMvc.perform(post("/api/v1/crm/contacts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "accountId":"%s",
                      "fullName":"Jane Contact",
                      "email":"jane.contact@acme.test",
                      "phone":"+91-9000000001",
                      "ownerUserId":"u-1001",
                      "customFields":{"tier":"Gold"}
                    }
                    """.formatted(accountId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.fullName").value("Jane Contact"))
            .andExpect(jsonPath("$.data.customFields.tier").value("Gold"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        final String contactId = readId(createContactResponse);

        mockMvc.perform(get("/api/v1/crm/contacts/{contactId}", contactId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.customFields.tier").value("Gold"));

        mockMvc.perform(put("/api/v1/crm/contacts/{contactId}", contactId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fullName":"Jane Contact Updated",
                      "customFields":{"tier":"Platinum"}
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.fullName").value("Jane Contact Updated"))
            .andExpect(jsonPath("$.data.customFields.tier").value("Platinum"));

        mockMvc.perform(get("/api/v1/crm/contacts")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items").isArray());

        mockMvc.perform(delete("/api/v1/crm/contacts/{contactId}", contactId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(delete("/api/v1/crm/accounts/{accountId}", accountId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    private String readId(final String responseBody) throws Exception {
        final JsonNode root = objectMapper.readTree(responseBody);
        return root.path("data").path("id").asText();
    }

    private String bearerToken(
        final String tenantCode,
        final List<String> roles,
        final Set<String> products,
        final java.util.Map<String, String> productRoles
    ) {
        final SecretKey key = Keys.hmacShaKeyFor("test-jwt-secret-test-jwt-secret-test-jwt".getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
            .subject("crm-admin@nexra.test")
            .claim("uid", UUID.randomUUID().toString())
            .claim("tenant", tenantCode)
            .claim("roles", roles)
            .claim("products", products)
            .claim("product_roles", productRoles)
            .signWith(key)
            .compact();
    }
}
