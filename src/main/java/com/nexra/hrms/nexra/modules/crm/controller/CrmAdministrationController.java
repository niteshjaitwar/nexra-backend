package com.nexra.hrms.nexra.modules.crm.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.exception.NexraForbiddenException;
import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmCustomFieldDefinitionRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmRecordSharingRuleRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmWorkflowRuleRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.IntegrationWebhookSignatureVerificationRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.IntegrationWebhookSubscriptionRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmCustomFieldDefinition;
import com.nexra.hrms.nexra.modules.crm.model.CrmRecordSharingRule;
import com.nexra.hrms.nexra.modules.crm.model.CrmWorkflowRule;
import com.nexra.hrms.nexra.modules.crm.model.IntegrationWebhookDeliveryAlertStatus;
import com.nexra.hrms.nexra.modules.crm.model.IntegrationWebhookDelivery;
import com.nexra.hrms.nexra.modules.crm.model.IntegrationWebhookDeliveryMetrics;
import com.nexra.hrms.nexra.modules.crm.model.IntegrationWebhookReplayAuditView;
import com.nexra.hrms.nexra.modules.crm.model.IntegrationWebhookSignatureVerification;
import com.nexra.hrms.nexra.modules.crm.model.IntegrationWebhookSubscription;
import com.nexra.hrms.nexra.modules.crm.service.CrmAdministrationService;
import com.nexra.hrms.nexra.modules.crm.support.CrmRequestContextResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "CRM Administration", description = "Tenant-scoped CRM customization, automation, sharing, and integration configuration.")
@Validated
@RestController
@RequestMapping("/api/v1/crm/admin")
@RequiredArgsConstructor
public class CrmAdministrationController {

    private final CrmAdministrationService service;
    private final CrmProperties properties;
    private final CrmRequestContextResolver requestContextResolver;

    @Operation(summary = "Create CRM custom field")
    @PostMapping("/custom-fields")
    public ResponseEntity<ApiResponse<CrmCustomFieldDefinition>> createCustomField(
        @Valid @RequestBody final CrmCustomFieldDefinitionRequest request
    ) {
        final JwtPrincipal principal = requirePrincipal();
        return ResponseEntity.status(201).body(ApiResponse.created(
            service.createCustomField(principal.tenantCode(), principal.email(), principal.userId().toString(), request),
            "CRM custom field created successfully."
        ));
    }

    @Operation(summary = "List CRM custom fields")
    @GetMapping("/custom-fields")
    public ResponseEntity<ApiResponse<List<CrmCustomFieldDefinition>>> listCustomFields(
        @RequestParam @NotBlank final String moduleKey
    ) {
        final JwtPrincipal principal = requirePrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
            service.listCustomFields(principal.tenantCode(), moduleKey),
            "CRM custom fields listed successfully."
        ));
    }

    @Operation(summary = "Create CRM workflow rule")
    @PostMapping("/workflow-rules")
    public ResponseEntity<ApiResponse<CrmWorkflowRule>> createWorkflowRule(
        @Valid @RequestBody final CrmWorkflowRuleRequest request
    ) {
        final JwtPrincipal principal = requirePrincipal();
        return ResponseEntity.status(201).body(ApiResponse.created(
            service.createWorkflowRule(principal.tenantCode(), principal.email(), principal.userId().toString(), request),
            "CRM workflow rule created successfully."
        ));
    }

    @Operation(summary = "List CRM workflow rules")
    @GetMapping("/workflow-rules")
    public ResponseEntity<ApiResponse<List<CrmWorkflowRule>>> listWorkflowRules(
        @RequestParam @NotBlank final String moduleKey
    ) {
        final JwtPrincipal principal = requirePrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
            service.listWorkflowRules(principal.tenantCode(), moduleKey),
            "CRM workflow rules listed successfully."
        ));
    }

    @Operation(summary = "Create CRM record sharing rule")
    @PostMapping("/sharing-rules")
    public ResponseEntity<ApiResponse<CrmRecordSharingRule>> createSharingRule(
        @Valid @RequestBody final CrmRecordSharingRuleRequest request
    ) {
        final JwtPrincipal principal = requirePrincipal();
        return ResponseEntity.status(201).body(ApiResponse.created(
            service.createRecordSharingRule(principal.tenantCode(), principal.email(), principal.userId().toString(), request),
            "CRM record sharing rule created successfully."
        ));
    }

    @Operation(summary = "List CRM record sharing rules")
    @GetMapping("/sharing-rules")
    public ResponseEntity<ApiResponse<List<CrmRecordSharingRule>>> listSharingRules(
        @RequestParam @NotBlank final String moduleKey
    ) {
        final JwtPrincipal principal = requirePrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
            service.listRecordSharingRules(principal.tenantCode(), moduleKey),
            "CRM record sharing rules listed successfully."
        ));
    }

    @Operation(summary = "Create CRM webhook subscription")
    @PostMapping("/webhooks")
    public ResponseEntity<ApiResponse<IntegrationWebhookSubscription>> createWebhook(
        @Valid @RequestBody final IntegrationWebhookSubscriptionRequest request
    ) {
        final JwtPrincipal principal = requirePrincipal();
        return ResponseEntity.status(201).body(ApiResponse.created(
            service.createWebhook(principal.tenantCode(), principal.email(), principal.userId().toString(), request),
            "CRM webhook subscription created successfully."
        ));
    }

    @Operation(summary = "List CRM webhook subscriptions")
    @GetMapping("/webhooks")
    public ResponseEntity<ApiResponse<List<IntegrationWebhookSubscription>>> listWebhooks() {
        final JwtPrincipal principal = requirePrincipal();
        return ResponseEntity.ok(ApiResponse.ok(service.listWebhooks(principal.tenantCode()), "CRM webhook subscriptions listed successfully."));
    }

    @Operation(summary = "List CRM webhook deliveries")
    @GetMapping("/webhooks/deliveries")
    public ResponseEntity<ApiResponse<List<IntegrationWebhookDelivery>>> listWebhookDeliveries() {
        final JwtPrincipal principal = requirePrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
            service.listWebhookDeliveries(principal.tenantCode()),
            "CRM webhook deliveries listed successfully."
        ));
    }

    @Operation(summary = "Replay dead-letter CRM webhook delivery")
    @PostMapping("/webhooks/deliveries/{deliveryId}/replay")
    public ResponseEntity<ApiResponse<IntegrationWebhookDelivery>> replayWebhookDelivery(
        @PathVariable final String deliveryId
    ) {
        final JwtPrincipal principal = requirePrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
            service.replayWebhookDelivery(principal.tenantCode(), principal.email(), principal.userId().toString(), deliveryId),
            "CRM webhook delivery replay queued successfully."
        ));
    }

    @Operation(summary = "Get CRM webhook delivery metrics")
    @GetMapping("/webhooks/deliveries/metrics")
    public ResponseEntity<ApiResponse<IntegrationWebhookDeliveryMetrics>> getWebhookDeliveryMetrics() {
        final JwtPrincipal principal = requirePrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
            service.getWebhookDeliveryMetrics(principal.tenantCode()),
            "CRM webhook delivery metrics fetched successfully."
        ));
    }

    @Operation(summary = "Get CRM webhook delivery alert status")
    @GetMapping("/webhooks/deliveries/alerts")
    public ResponseEntity<ApiResponse<IntegrationWebhookDeliveryAlertStatus>> getWebhookDeliveryAlertStatus() {
        final JwtPrincipal principal = requirePrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
            service.getWebhookDeliveryAlertStatus(principal.tenantCode()),
            "CRM webhook delivery alert status fetched successfully."
        ));
    }

    @Operation(summary = "List CRM webhook replay audits")
    @GetMapping("/webhooks/replays")
    public ResponseEntity<ApiResponse<List<IntegrationWebhookReplayAuditView>>> listWebhookReplayAudits(
        @RequestParam(defaultValue = "50") final int limit
    ) {
        final JwtPrincipal principal = requirePrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
            service.listWebhookReplayAudits(principal.tenantCode(), limit),
            "CRM webhook replay audits listed successfully."
        ));
    }

    @Operation(summary = "Verify CRM webhook signature")
    @PostMapping("/webhooks/signature/verify")
    public ResponseEntity<ApiResponse<IntegrationWebhookSignatureVerification>> verifyWebhookSignature(
        @Valid @RequestBody final IntegrationWebhookSignatureVerificationRequest request
    ) {
        final JwtPrincipal principal = requirePrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
            service.verifyWebhookSignature(principal.tenantCode(), request),
            "CRM webhook signature verified successfully."
        ));
    }

    private JwtPrincipal requirePrincipal() {
        final JwtPrincipal principal = requestContextResolver.resolveAuthenticatedPrincipal(properties);
        requireCrmAdmin(principal);
        return principal;
    }

    private void requireCrmAdmin(final JwtPrincipal principal) {
        if (!principal.products().contains("CRM")) {
            throw new NexraForbiddenException("User does not have CRM product access.");
        }
        final String crmProductRole = principal.productRoles().get("CRM");
        if (principal.roles().contains("ROLE_PLATFORM_ADMIN")
            || "TENANT_ADMIN".equals(crmProductRole)
            || "SALES_MANAGER".equals(crmProductRole)) {
            return;
        }
        throw new NexraForbiddenException("CRM administration requires CRM tenant admin or sales manager permission.");
    }
}
