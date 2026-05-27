package com.nexra.hrms.nexra.modules.crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.nexra.hrms.nexra.modules.crm.repository.IntegrationWebhookDeliveryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "nexra.crm.enforce-auth=true",
    "nexra.crm.webhooks.dispatch-fixed-delay-ms=600000"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CrmAdministrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private IntegrationWebhookDeliveryRepository webhookDeliveryRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void crmAdminCanConfigureCustomizationAutomationSharingAndWebhooks() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_USER"), Set.of("CRM"), Map.of("CRM", "SALES_MANAGER"));
        final String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        final String fieldKey = "renewal_score_" + suffix;

        mockMvc.perform(post("/api/v1/crm/admin/custom-fields")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "moduleKey":"crm-deals",
                      "fieldKey":"%s",
                      "label":"Renewal Score",
                      "fieldType":"NUMBER",
                      "required":false,
                      "searchable":true,
                      "validationJson":"{\\"min\\":0,\\"max\\":100}",
                      "active":true
                    }
                    """.formatted(fieldKey)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.moduleKey").value("crm-deals"))
            .andExpect(jsonPath("$.data.fieldKey").value(fieldKey));

        mockMvc.perform(get("/api/v1/crm/admin/custom-fields")
                .header("Authorization", "Bearer " + token)
                .param("moduleKey", "crm-deals"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].moduleKey").value("crm-deals"));

        mockMvc.perform(get("/api/v1/crm/admin/custom-fields")
                .header("Authorization", "Bearer " + token)
                .param("moduleKey", "OPPORTUNITIES"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/crm/admin/workflow-rules")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "moduleKey":"crm-deals",
                      "name":"Notify on won deal",
                      "triggerEvent":"RECORD_UPDATED",
                      "criteriaJson":"{\\"field\\":\\"stage\\",\\"equals\\":\\"WON\\"}",
                      "actionsJson":"[{\\"type\\":\\"WEBHOOK\\",\\"eventType\\":\\"CRM_DEAL_WON\\"}]",
                      "priority":10,
                      "active":true
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.triggerEvent").value("RECORD_UPDATED"));

        mockMvc.perform(get("/api/v1/crm/admin/workflow-rules")
                .header("Authorization", "Bearer " + token)
                .param("moduleKey", "crm-deals"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].name").value("Notify on won deal"));

        mockMvc.perform(post("/api/v1/crm/admin/sharing-rules")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "moduleKey":"crm-deals",
                      "name":"Sales managers can edit enterprise deals",
                      "criteriaJson":"{\\"segment\\":\\"ENTERPRISE\\"}",
                      "principalType":"ROLE",
                      "principalKey":"ROLE_SALES_MANAGER",
                      "accessLevel":"EDIT",
                      "active":true
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.accessLevel").value("EDIT"));

        mockMvc.perform(get("/api/v1/crm/admin/sharing-rules")
                .header("Authorization", "Bearer " + token)
                .param("moduleKey", "crm-deals"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].principalType").value("ROLE"));

        mockMvc.perform(post("/api/v1/crm/admin/webhooks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "eventType":"CRM_DEAL_WON",
                      "targetUrl":"https://integrations.acme.test/crm/deal-won",
                      "secret":"super-secret-webhook-token",
                      "active":true
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.eventType").value("CRM_DEAL_WON"))
            .andExpect(jsonPath("$.data.targetUrl").value("https://integrations.acme.test/crm/deal-won"));

        org.junit.jupiter.api.Assertions.assertTrue(
            webhookDeliveryRepository.countByTenantCodeIgnoreCaseAndProductKeyIgnoreCase("ACME", "CRM") >= 1
        );

        mockMvc.perform(get("/api/v1/crm/admin/webhooks")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].productKey").value("CRM"));

        mockMvc.perform(get("/api/v1/crm/admin/webhooks/deliveries")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].status").isNotEmpty());

        final String deliveriesPayload = mockMvc.perform(get("/api/v1/crm/admin/webhooks/deliveries")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        final String deliveryId = objectMapper.readTree(deliveriesPayload).path("data").get(0).path("id").asText();

        mockMvc.perform(post("/api/v1/crm/admin/webhooks/deliveries/{deliveryId}/replay", deliveryId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().is(422))
            .andExpect(jsonPath("$.message").value("Only dead-letter webhook deliveries can be replayed."));

        mockMvc.perform(get("/api/v1/crm/admin/webhooks/deliveries/metrics")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalCount").isNumber());

        mockMvc.perform(get("/api/v1/crm/admin/webhooks/deliveries/alerts")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.deadLetterThreshold").isNumber())
            .andExpect(jsonPath("$.data.retryingThreshold").isNumber());

        mockMvc.perform(get("/api/v1/crm/admin/webhooks/replays")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());

        final String payload = "{\"type\":\"CRM_DEAL_WON\",\"dealId\":\"D-1001\"}";
        final String idempotencyKey = "evt-01-abc";
        final String timestamp = "1760000000";
        final String secret = "super-secret-webhook-token";
        final String signature = signature(secret, payload, idempotencyKey, timestamp);

        mockMvc.perform(post("/api/v1/crm/admin/webhooks/signature/verify")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "payloadJson":"%s",
                      "idempotencyKey":"%s",
                      "timestamp":"%s",
                      "secret":"%s",
                      "signature":"%s"
                    }
                    """.formatted(escapeJson(payload), idempotencyKey, timestamp, secret, signature)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.valid").value(true));

        mockMvc.perform(post("/api/v1/crm/admin/webhooks/signature/verify")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "payloadJson":"%s",
                      "idempotencyKey":"%s",
                      "timestamp":"%s",
                      "secret":"%s",
                      "signature":"%s"
                    }
                    """.formatted(escapeJson(payload), idempotencyKey, timestamp, secret, "invalid-signature")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.valid").value(false));
    }

    @Test
    void crmAdministrationRequiresCrmAdminRole() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_USER"), Set.of("CRM"), Map.of("CRM", "SALES_REP"));

        mockMvc.perform(get("/api/v1/crm/admin/custom-fields")
                .header("Authorization", "Bearer " + token)
                .param("moduleKey", "crm-deals"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("CRM administration requires CRM tenant admin or sales manager permission."));
    }

    @Test
    void rejectsUnsafeWebhookPorts() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_USER"), Set.of("CRM"), Map.of("CRM", "SALES_MANAGER"));

        mockMvc.perform(post("/api/v1/crm/admin/webhooks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "eventType":"CRM_DEAL_WON",
                      "targetUrl":"https://integrations.acme.test:8080/crm/deal-won",
                      "secret":"super-secret-webhook-token",
                      "active":true
                    }
                    """))
            .andExpect(status().is(422))
            .andExpect(jsonPath("$.message").value("Webhook targetUrl may only use port 80 or 443."));
    }

    @Test
    void rejectsLocalAndPrivateWebhookHosts() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_USER"), Set.of("CRM"), Map.of("CRM", "SALES_MANAGER"));

        mockMvc.perform(post("/api/v1/crm/admin/webhooks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "eventType":"CRM_DEAL_WON",
                      "targetUrl":"http://localhost/crm/deal-won",
                      "secret":"super-secret-webhook-token",
                      "active":true
                    }
                    """))
            .andExpect(status().is(422))
            .andExpect(jsonPath("$.message").value("Webhook targetUrl host is not allowed."));

        mockMvc.perform(post("/api/v1/crm/admin/webhooks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "eventType":"CRM_DEAL_WON",
                      "targetUrl":"http://10.0.0.10/crm/deal-won",
                      "secret":"super-secret-webhook-token",
                      "active":true
                    }
                    """))
            .andExpect(status().is(422))
            .andExpect(jsonPath("$.message").value("Webhook targetUrl host is not allowed."));
    }

    private String bearerToken(
        final String tenantCode,
        final List<String> roles,
        final Set<String> products,
        final Map<String, String> productRoles
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

    private String signature(
        final String secret,
        final String payload,
        final String idempotencyKey,
        final String timestamp
    ) {
        final String secretHash = sha256Hex(secret);
        return sha256Hex(secretHash + "." + payload + "." + idempotencyKey + "." + timestamp);
    }

    private String sha256Hex(final String value) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 digest is unavailable.", ex);
        }
    }

    private String escapeJson(final String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
