package com.nexra.hrms.nexra.modules.crm.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.common.security.NexraPermission;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmCaseAssignRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmCaseCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmCaseStatusUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmCase;
import com.nexra.hrms.nexra.modules.crm.service.CrmCaseService;
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

@Tag(name = "CRM Cases", description = "Support case management APIs with a configurable status state machine.")
@RestController
@RequestMapping("/api/v1/crm/cases")
@RequiredArgsConstructor
public class CrmCaseController {

    private final CrmCaseService caseService;
    private final CrmProperties properties;
    private final CrmRequestContextResolver requestContextResolver;

    @Operation(summary = "Create CRM case")
    @PostMapping
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_WRITE + "')")
    public ResponseEntity<ApiResponse<CrmCase>> create(@Valid @RequestBody final CrmCaseCreateRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created(
            caseService.create(resolveTenantCode(), resolveActorEmail(), request),
            "CRM case created successfully."
        ));
    }

    @Operation(summary = "Get CRM case")
    @GetMapping("/{caseId}")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_READ + "')")
    public ResponseEntity<ApiResponse<CrmCase>> getById(@PathVariable final String caseId) {
        return ResponseEntity.ok(ApiResponse.ok(
            caseService.findById(resolveTenantCode(), caseId),
            "CRM case fetched successfully."
        ));
    }

    @Operation(summary = "List CRM cases")
    @GetMapping
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_READ + "')")
    public ResponseEntity<ApiResponse<PageResponse<CrmCase>>> list(
        @RequestParam(defaultValue = "0") final int page,
        @RequestParam(defaultValue = "20") final int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            caseService.list(resolveTenantCode(), page, size),
            "CRM cases listed successfully."
        ));
    }

    @Operation(summary = "Transition CRM case status", description = "Moves a case to a new status if permitted by the configured state machine.")
    @PostMapping("/{caseId}/status")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_WRITE + "')")
    public ResponseEntity<ApiResponse<CrmCase>> transitionStatus(
        @PathVariable final String caseId,
        @Valid @RequestBody final CrmCaseStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            caseService.transitionStatus(resolveTenantCode(), resolveActorEmail(), caseId, request),
            "CRM case status updated successfully."
        ));
    }

    @Operation(summary = "Reassign CRM case", description = "Assigns case ownership to a different user.")
    @PostMapping("/{caseId}/assign")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_WRITE + "')")
    public ResponseEntity<ApiResponse<CrmCase>> assign(
        @PathVariable final String caseId,
        @Valid @RequestBody final CrmCaseAssignRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            caseService.assign(resolveTenantCode(), resolveActorEmail(), caseId, request),
            "CRM case reassigned successfully."
        ));
    }

    private String resolveTenantCode() {
        return requestContextResolver.resolveTenantCode(properties);
    }

    private String resolveActorEmail() {
        return requestContextResolver.resolveActorEmail();
    }
}
