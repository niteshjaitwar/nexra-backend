package com.nexra.hrms.nexra.modules.crm;

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
class CrmLeadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void crmLeadCrudFlowWorksWithTenantIsolation() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_CRM_ADMIN"), Set.of("CRM"));

        final String createResponse = mockMvc.perform(post("/api/v1/crm/leads")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fullName":"Aarav Singh",
                      "email":"aarav.singh@acme.test",
                      "phone":"+91-9000000000",
                      "company":"Acme Corp",
                      "source":"Website",
                      "ownerUserId":"u-1001",
                      "notes":"Warm inbound lead"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.tenantCode").value("ACME"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        final String leadId = readLeadId(createResponse);

        mockMvc.perform(post("/api/v1/crm/activities")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "leadId":"%s",
                      "activityType":"CALL",
                      "notes":"Discovery call completed",
                      "ownerUserId":"u-1001"
                    }
                    """.formatted(leadId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.leadId").value(leadId))
            .andExpect(jsonPath("$.data.activityType").value("CALL"));

        mockMvc.perform(get("/api/v1/crm/activities")
                .header("Authorization", "Bearer " + token)
                .param("leadId", leadId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalItems").value(1))
            .andExpect(jsonPath("$.data.items[0].notes").value("Discovery call completed"));

        mockMvc.perform(get("/api/v1/crm/leads")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalItems").value(1))
            .andExpect(jsonPath("$.data.items[0].tenantCode").value("ACME"));

        mockMvc.perform(put("/api/v1/crm/leads/{leadId}", leadId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "company":"Acme Enterprise",
                      "notes":"Qualified by SDR"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.company").value("Acme Enterprise"));

        mockMvc.perform(post("/api/v1/crm/leads/{leadId}/convert", leadId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.leadId").value(leadId))
            .andExpect(jsonPath("$.data.accountId").isNotEmpty())
            .andExpect(jsonPath("$.data.contactId").isNotEmpty())
            .andExpect(jsonPath("$.data.dealId").isNotEmpty());

        mockMvc.perform(delete("/api/v1/crm/leads/{leadId}", leadId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().is(422))
            .andExpect(jsonPath("$.success").value(false));

        final String createForDeleteResponse = mockMvc.perform(post("/api/v1/crm/leads")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fullName":"Delete Candidate",
                      "email":"delete.candidate@acme.test",
                      "phone":"+91-9444444444",
                      "company":"Acme Corp",
                      "source":"Website",
                      "ownerUserId":"u-1001",
                      "notes":"To be deleted"
                    }
                    """))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        final String deletableLeadId = readLeadId(createForDeleteResponse);

        mockMvc.perform(delete("/api/v1/crm/leads/{leadId}", deletableLeadId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void rejectsMissingCrmProductScope() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_HR_ADMIN"), Set.of("HRMS"));

        mockMvc.perform(get("/api/v1/crm/leads")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("User does not have CRM product access."));
    }

    @Test
    void hidesTenantDataAcrossTokens() throws Exception {
        final String acmeToken = bearerToken("ACME", List.of("ROLE_CRM_ADMIN"), Set.of("CRM"));
        final String betaToken = bearerToken("BETA", List.of("ROLE_CRM_ADMIN"), Set.of("CRM"));

        final String createResponse = mockMvc.perform(post("/api/v1/crm/leads")
                .header("Authorization", "Bearer " + acmeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fullName":"Riya Patel",
                      "email":"riya@acme.test",
                      "phone":"+91-9111111111",
                      "company":"Acme Corp",
                      "source":"Referral",
                      "ownerUserId":"u-1002",
                      "notes":"Needs demo"
                    }
                    """))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        final String leadId = readLeadId(createResponse);

        mockMvc.perform(get("/api/v1/crm/leads/{leadId}", leadId)
                .header("Authorization", "Bearer " + betaToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void nonPrivilegedUsersAreOwnerScopedWithinSameTenant() throws Exception {
        final String ownerId = UUID.randomUUID().toString();
        final String otherUserId = UUID.randomUUID().toString();
        final String adminToken = bearerTokenWithUid("ACME", ownerId, List.of("ROLE_CRM_ADMIN"), Set.of("CRM"));
        final String salesRepToken = bearerTokenWithUid("ACME", otherUserId, List.of("ROLE_USER"), Set.of("CRM"));

        final String createResponse = mockMvc.perform(post("/api/v1/crm/leads")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fullName":"Scoped Lead",
                      "email":"scoped.lead@acme.test",
                      "phone":"+91-9222222222",
                      "company":"Acme Corp",
                      "source":"Email",
                      "ownerUserId":"%s",
                      "notes":"Owner-only visibility"
                    }
                    """.formatted(ownerId)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        final String leadId = readLeadId(createResponse);

        mockMvc.perform(get("/api/v1/crm/leads/{leadId}", leadId)
                .header("Authorization", "Bearer " + salesRepToken))
            .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/crm/leads")
                .header("Authorization", "Bearer " + salesRepToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalItems").value(0));

        mockMvc.perform(put("/api/v1/crm/leads/{leadId}", leadId)
                .header("Authorization", "Bearer " + salesRepToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "notes":"Unauthorized update attempt"
                    }
                    """))
            .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/v1/crm/leads/{leadId}", leadId)
                .header("Authorization", "Bearer " + salesRepToken))
            .andExpect(status().isNotFound());
    }

    @Test
    void nonPrivilegedUsersCannotCreateLeadForAnotherOwner() throws Exception {
        final String actorUserId = UUID.randomUUID().toString();
        final String differentOwnerId = UUID.randomUUID().toString();
        final String salesRepToken = bearerTokenWithUid("ACME", actorUserId, List.of("ROLE_USER"), Set.of("CRM"));

        mockMvc.perform(post("/api/v1/crm/leads")
                .header("Authorization", "Bearer " + salesRepToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fullName":"Invalid Owner Lead",
                      "email":"invalid.owner@acme.test",
                      "phone":"+91-9333333333",
                      "company":"Acme Corp",
                      "source":"Phone",
                      "ownerUserId":"%s",
                      "notes":"Should be blocked"
                    }
                    """.formatted(differentOwnerId)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("Non-admin CRM users can only operate on their own owned records."));
    }

    private String bearerToken(final String tenantCode, final List<String> roles, final Set<String> products) {
        return bearerTokenWithUid(tenantCode, UUID.randomUUID().toString(), roles, products);
    }

    private String bearerTokenWithUid(
        final String tenantCode,
        final String uid,
        final List<String> roles,
        final Set<String> products
    ) {
        final SecretKey key = Keys.hmacShaKeyFor("test-jwt-secret-test-jwt-secret-test-jwt".getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
            .subject("crm-admin@nexra.test")
            .claim("uid", uid)
            .claim("tenant", tenantCode)
            .claim("roles", roles)
            .claim("products", products)
            .signWith(key)
            .compact();
    }

    private String readLeadId(final String payload) throws Exception {
        return objectMapper.readTree(payload).path("data").path("id").asText();
    }
}
