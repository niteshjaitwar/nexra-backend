package com.nexra.hrms.nexra.modules.crm.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.common.security.NexraPermission;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmCampaignCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmCampaignStatusUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmLeadCreateRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmCampaign;
import com.nexra.hrms.nexra.modules.crm.model.CrmLead;
import com.nexra.hrms.nexra.modules.crm.service.CrmCampaignService;
import com.nexra.hrms.nexra.modules.crm.support.CrmRequestContextResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "CRM Campaigns", description = "Marketing campaign APIs with a configurable lifecycle state machine.")
@RestController
@RequestMapping("/api/v1/crm/campaigns")
@RequiredArgsConstructor
public class CrmCampaignController {

    private final CrmCampaignService campaignService;
    private final CrmProperties properties;
    private final CrmRequestContextResolver requestContextResolver;

    @Operation(summary = "Create CRM campaign")
    @PostMapping
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_WRITE + "')")
    public ResponseEntity<ApiResponse<CrmCampaign>> create(@Valid @RequestBody final CrmCampaignCreateRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created(
            campaignService.create(resolveTenantCode(), resolveActorEmail(), request),
            "CRM campaign created successfully."
        ));
    }

    @Operation(summary = "Get CRM campaign")
    @GetMapping("/{campaignId}")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_READ + "')")
    public ResponseEntity<ApiResponse<CrmCampaign>> getById(@PathVariable final String campaignId) {
        return ResponseEntity.ok(ApiResponse.ok(
            campaignService.findById(resolveTenantCode(), campaignId),
            "CRM campaign fetched successfully."
        ));
    }

    @Operation(summary = "List CRM campaigns")
    @GetMapping
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_READ + "')")
    public ResponseEntity<ApiResponse<PageResponse<CrmCampaign>>> list(
        @RequestParam(defaultValue = "0") final int page,
        @RequestParam(defaultValue = "20") final int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            campaignService.list(resolveTenantCode(), page, size),
            "CRM campaigns listed successfully."
        ));
    }

    @Operation(summary = "Transition CRM campaign status",
        description = "Moves a campaign to a new status if permitted by the configured state machine.")
    @PostMapping("/{campaignId}/status")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_WRITE + "')")
    public ResponseEntity<ApiResponse<CrmCampaign>> transitionStatus(
        @PathVariable final String campaignId,
        @Valid @RequestBody final CrmCampaignStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            campaignService.transitionStatus(resolveTenantCode(), resolveActorEmail(), campaignId, request),
            "CRM campaign status updated successfully."
        ));
    }

    @Operation(summary = "Capture campaign-attributed lead",
        description = "Registers a lead against an active campaign for closed-loop marketing attribution.")
    @PostMapping("/{campaignId}/leads")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_WRITE + "')")
    public ResponseEntity<ApiResponse<CrmLead>> captureLead(
        @PathVariable final String campaignId,
        @Valid @RequestBody final CrmLeadCreateRequest request
    ) {
        return ResponseEntity.status(201).body(ApiResponse.created(
            campaignService.captureLead(
                resolveTenantCode(),
                campaignId,
                resolveActorEmail(),
                request,
                requestContextResolver.resolveLeadAccessScope(properties)
            ),
            "Campaign lead captured successfully."
        ));
    }

    @Operation(summary = "List campaign-attributed leads")
    @GetMapping("/{campaignId}/leads")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_READ + "')")
    public ResponseEntity<ApiResponse<PageResponse<CrmLead>>> listLeads(
        @PathVariable final String campaignId,
        @RequestParam(defaultValue = "0") final int page,
        @RequestParam(defaultValue = "20") final int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            campaignService.listCampaignLeads(
                resolveTenantCode(),
                campaignId,
                page,
                size,
                requestContextResolver.resolveLeadAccessScope(properties)
            ),
            "Campaign leads listed successfully."
        ));
    }

    private String resolveTenantCode() {
        return requestContextResolver.resolveTenantCode(properties);
    }

    private String resolveActorEmail() {
        return requestContextResolver.resolveActorEmail();
    }
}
