package com.nexra.hrms.nexra.modules.crm.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.exception.NexraForbiddenException;
import com.nexra.hrms.nexra.common.exception.NexraUnauthorizedException;
import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmCustomFieldDefinitionRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmRecordSharingRuleRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmWorkflowRuleRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.IntegrationWebhookSubscriptionRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmCustomFieldDefinition;
import com.nexra.hrms.nexra.modules.crm.model.CrmRecordSharingRule;
import com.nexra.hrms.nexra.modules.crm.model.CrmWorkflowRule;
import com.nexra.hrms.nexra.modules.crm.model.IntegrationWebhookSubscription;
import com.nexra.hrms.nexra.modules.crm.service.CrmAdministrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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

    private JwtPrincipal requirePrincipal() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal) {
            requireCrmAdmin(principal);
            if (!StringUtils.hasText(principal.tenantCode())) {
                throw new NexraUnauthorizedException("Authenticated CRM user is missing tenant context.");
            }
            return principal;
        }
        if (!properties.isEnforceAuth()) {
            throw new NexraUnauthorizedException("CRM administration requires authentication.");
        }
        throw new NexraUnauthorizedException("Authentication is required.");
    }

    private void requireCrmAdmin(final JwtPrincipal principal) {
        if (!principal.products().contains("CRM")) {
            throw new NexraForbiddenException("User does not have CRM product access.");
        }
        if (principal.roles().contains("ROLE_CRM_ADMIN") || principal.roles().contains("ROLE_PLATFORM_ADMIN")) {
            return;
        }
        throw new NexraForbiddenException("CRM administration requires CRM admin permission.");
    }
}
