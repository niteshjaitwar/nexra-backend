package com.nexra.hrms.nexra.modules.crm;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "nexra.crm.enforce-auth=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CrmAdministrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void crmAdminCanConfigureCustomizationAutomationSharingAndWebhooks() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_CRM_ADMIN"), Set.of("CRM"));
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

        mockMvc.perform(get("/api/v1/crm/admin/webhooks")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].productKey").value("CRM"));
    }

    @Test
    void crmAdministrationRequiresCrmAdminRole() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_CRM_USER"), Set.of("CRM"));

        mockMvc.perform(get("/api/v1/crm/admin/custom-fields")
                .header("Authorization", "Bearer " + token)
                .param("moduleKey", "crm-deals"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("CRM administration requires CRM admin permission."));
    }

    @Test
    void rejectsUnsafeWebhookPorts() throws Exception {
        final String token = bearerToken("ACME", List.of("ROLE_CRM_ADMIN"), Set.of("CRM"));

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
}
