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
class CrmCampaignQuoteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void campaignLifecycleAndValidationWorks() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_USER"), Set.of("CRM"));

        final String createResponse = mockMvc.perform(post("/api/v1/crm/campaigns")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":"Spring Launch",
                      "campaignType":"EMAIL",
                      "budget":50000,
                      "ownerUserId":"u-1"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.status").value("DRAFT"))
            .andExpect(jsonPath("$.data.campaignType").value("EMAIL"))
            .andReturn().getResponse().getContentAsString();
        final String campaignId = readId(createResponse);

        mockMvc.perform(post("/api/v1/crm/campaigns/{id}/status", campaignId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetStatus\":\"ACTIVE\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // DRAFT is not reachable from ACTIVE.
        mockMvc.perform(post("/api/v1/crm/campaigns/{id}/status", campaignId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetStatus\":\"DRAFT\"}"))
            .andExpect(status().is(422));

        mockMvc.perform(post("/api/v1/crm/campaigns/{id}/status", campaignId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetStatus\":\"COMPLETED\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        // Unsupported campaign type is rejected.
        mockMvc.perform(post("/api/v1/crm/campaigns")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":"Bad type",
                      "campaignType":"TELEPATHY",
                      "ownerUserId":"u-1"
                    }
                    """))
            .andExpect(status().is(422));
    }

    @Test
    void quoteTotalsAndLifecycleWorks() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_USER"), Set.of("CRM"));

        final String createResponse = mockMvc.perform(post("/api/v1/crm/quotes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title":"Enterprise Plan Quote",
                      "ownerUserId":"u-1",
                      "lineItems":[
                        {"productName":"Seats","quantity":2,"unitPrice":1000,"discountPercent":10,"taxPercent":18},
                        {"productName":"Onboarding","quantity":1,"unitPrice":500,"discountPercent":0,"taxPercent":18}
                      ]
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.status").value("DRAFT"))
            .andExpect(jsonPath("$.data.subtotal").value(2500.00))
            .andExpect(jsonPath("$.data.discountTotal").value(200.00))
            .andExpect(jsonPath("$.data.taxTotal").value(414.00))
            .andExpect(jsonPath("$.data.grandTotal").value(2714.00))
            .andExpect(jsonPath("$.data.lineItems.length()").value(2))
            .andReturn().getResponse().getContentAsString();
        final String quoteId = readId(createResponse);

        mockMvc.perform(post("/api/v1/crm/quotes/{id}/status", quoteId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetStatus\":\"SENT\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("SENT"));

        mockMvc.perform(post("/api/v1/crm/quotes/{id}/status", quoteId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetStatus\":\"ACCEPTED\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("ACCEPTED"));

        // ACCEPTED is terminal; cannot go back to SENT.
        mockMvc.perform(post("/api/v1/crm/quotes/{id}/status", quoteId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetStatus\":\"SENT\"}"))
            .andExpect(status().is(422));
    }

    @Test
    void campaignLeadAttributionClosedLoop() throws Exception {
        final String ownerId = UUID.randomUUID().toString();
        final String token = bearerToken("CAMPAIGN_CO", ownerId, List.of("ROLE_USER"), Set.of("CRM"));

        final String createResponse = mockMvc.perform(post("/api/v1/crm/campaigns")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":"Spring Webinar",
                      "campaignType":"WEBINAR",
                      "ownerUserId":"%s"
                    }
                    """.formatted(ownerId)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        final String campaignId = readId(createResponse);

        mockMvc.perform(post("/api/v1/crm/campaigns/{id}/status", campaignId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetStatus\":\"ACTIVE\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/crm/campaigns/{id}/leads", campaignId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fullName":"Attendee One",
                      "email":"attendee.one@acme.test",
                      "company":"Acme Labs",
                      "ownerUserId":"%s"
                    }
                    """.formatted(ownerId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.campaignId").value(campaignId))
            .andExpect(jsonPath("$.data.source").value("CAMPAIGN:Spring Webinar"));

        mockMvc.perform(get("/api/v1/crm/campaigns/{id}/leads", campaignId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[0].campaignId").value(campaignId));
    }

    @Test
    void acceptedQuoteCreatesLinkedDeal() throws Exception {
        final String token = bearerToken("QUOTE_CO", List.of("ROLE_USER"), Set.of("CRM"));

        final String createResponse = mockMvc.perform(post("/api/v1/crm/quotes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title":"Enterprise Renewal",
                      "ownerUserId":"u-1",
                      "lineItems":[
                        {"productName":"Annual License","quantity":1,"unitPrice":50000,"discountPercent":0,"taxPercent":0}
                      ]
                    }
                    """))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        final String quoteId = readId(createResponse);

        mockMvc.perform(post("/api/v1/crm/quotes/{id}/status", quoteId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetStatus\":\"SENT\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/crm/quotes/{id}/status", quoteId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetStatus\":\"ACCEPTED\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.dealId").isNotEmpty())
            .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }

    @Test
    void acceptedQuoteAutoCreatesLinkedOpsProject() throws Exception {
        final String userId = UUID.randomUUID().toString();
        final String crmToken = bearerToken("OPS_QUOTE_CO", userId, List.of("ROLE_USER"), Set.of("CRM"));
        final String opsToken = bearerToken("OPS_QUOTE_CO", userId, List.of("ROLE_USER"), Set.of("OPS"));

        final String createResponse = mockMvc.perform(post("/api/v1/crm/quotes")
                .header("Authorization", "Bearer " + crmToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title":"Delivery Package",
                      "ownerUserId":"%s",
                      "lineItems":[
                        {"productName":"Implementation","quantity":1,"unitPrice":25000,"discountPercent":0,"taxPercent":0}
                      ]
                    }
                    """.formatted(userId)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        final String quoteId = readId(createResponse);

        mockMvc.perform(post("/api/v1/crm/quotes/{id}/status", quoteId)
                .header("Authorization", "Bearer " + crmToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetStatus\":\"SENT\"}"))
            .andExpect(status().isOk());

        final String acceptResponse = mockMvc.perform(post("/api/v1/crm/quotes/{id}/status", quoteId)
                .header("Authorization", "Bearer " + crmToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetStatus\":\"ACCEPTED\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.dealId").isNotEmpty())
            .andReturn().getResponse().getContentAsString();
        final String dealId = objectMapper.readTree(acceptResponse).path("data").path("dealId").asText();

        mockMvc.perform(get("/api/v1/operations/projects")
                .header("Authorization", "Bearer " + opsToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[?(@.crmDealId == '" + dealId + "')]").exists());
    }

    @Test
    void rejectsMissingCrmProductScopeForCampaigns() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_USER"), Set.of("HRMS"));

        mockMvc.perform(post("/api/v1/crm/campaigns")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":"Denied",
                      "campaignType":"EMAIL",
                      "ownerUserId":"u-1"
                    }
                    """))
            .andExpect(status().isForbidden());
    }

    @Test
    void enforcesTenantIsolationOnCampaignRead() throws Exception {
        final String acmeToken = bearerToken("ACME", List.of("ROLE_USER"), Set.of("CRM"));
        final String betaToken = bearerToken("BETA", List.of("ROLE_USER"), Set.of("CRM"));

        final String createResponse = mockMvc.perform(post("/api/v1/crm/campaigns")
                .header("Authorization", "Bearer " + acmeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":"Tenant scoped",
                      "campaignType":"EVENT",
                      "ownerUserId":"u-1"
                    }
                    """))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        final String campaignId = readId(createResponse);

        mockMvc.perform(get("/api/v1/crm/campaigns/{id}", campaignId)
                .header("Authorization", "Bearer " + betaToken))
            .andExpect(status().isNotFound());
    }

    @Test
    void dealStageTransitionFollowsConfiguredPipeline() throws Exception {
        final String userId = UUID.randomUUID().toString();
        final String token = bearerToken("DEAL_CO", userId, List.of("ROLE_USER"), Set.of("CRM"));

        final String createResponse = mockMvc.perform(post("/api/v1/crm/deals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title":"Enterprise License",
                      "stage":"PROSPECTING",
                      "valueAmount":120000,
                      "currency":"INR",
                      "ownerUserId":"%s"
                    }
                    """.formatted(userId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.stage").value("PROSPECTING"))
            .andReturn().getResponse().getContentAsString();
        final String dealId = readId(createResponse);

        mockMvc.perform(post("/api/v1/crm/deals/{id}/transition", dealId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetStage\":\"QUALIFICATION\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.stage").value("QUALIFICATION"));

        mockMvc.perform(post("/api/v1/crm/deals/{id}/transition", dealId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetStage\":\"PROSPECTING\"}"))
            .andExpect(status().is(422));
    }

    private String readId(final String responseBody) throws Exception {
        final JsonNode root = objectMapper.readTree(responseBody);
        return root.path("data").path("id").asText();
    }

    private String bearerToken(final String tenantCode, final List<String> roles, final Set<String> products) {
        return bearerToken(tenantCode, UUID.randomUUID().toString(), roles, products);
    }

    private String bearerToken(
        final String tenantCode,
        final String userId,
        final List<String> roles,
        final Set<String> products
    ) {
        final SecretKey key = Keys.hmacShaKeyFor("test-jwt-secret-test-jwt-secret-test-jwt".getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
            .subject("crm-user@nexra.test")
            .claim("uid", userId)
            .claim("tenant", tenantCode)
            .claim("roles", roles)
            .claim("products", products)
            .signWith(key)
            .compact();
    }
}
