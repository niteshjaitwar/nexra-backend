package com.nexra.hrms.nexra.modules.crm.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.common.security.NexraPermission;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmQuoteCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmQuoteStatusUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmQuote;
import com.nexra.hrms.nexra.modules.crm.service.CrmQuoteService;
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

@Tag(name = "CRM Quotes", description = "Sales quote APIs with line-item pricing and a configurable lifecycle state machine.")
@RestController
@RequestMapping("/api/v1/crm/quotes")
@RequiredArgsConstructor
public class CrmQuoteController {

    private final CrmQuoteService quoteService;
    private final CrmProperties properties;
    private final CrmRequestContextResolver requestContextResolver;

    @Operation(summary = "Create CRM quote")
    @PostMapping
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_WRITE + "')")
    public ResponseEntity<ApiResponse<CrmQuote>> create(@Valid @RequestBody final CrmQuoteCreateRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created(
            quoteService.create(resolveTenantCode(), resolveActorEmail(), request),
            "CRM quote created successfully."
        ));
    }

    @Operation(summary = "Get CRM quote")
    @GetMapping("/{quoteId}")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_READ + "')")
    public ResponseEntity<ApiResponse<CrmQuote>> getById(@PathVariable final String quoteId) {
        return ResponseEntity.ok(ApiResponse.ok(
            quoteService.findById(resolveTenantCode(), quoteId),
            "CRM quote fetched successfully."
        ));
    }

    @Operation(summary = "List CRM quotes")
    @GetMapping
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_READ + "')")
    public ResponseEntity<ApiResponse<PageResponse<CrmQuote>>> list(
        @RequestParam(defaultValue = "0") final int page,
        @RequestParam(defaultValue = "20") final int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            quoteService.list(resolveTenantCode(), page, size),
            "CRM quotes listed successfully."
        ));
    }

    @Operation(summary = "Transition CRM quote status",
        description = "Moves a quote to a new status if permitted by the configured state machine.")
    @PostMapping("/{quoteId}/status")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_WRITE + "')")
    public ResponseEntity<ApiResponse<CrmQuote>> transitionStatus(
        @PathVariable final String quoteId,
        @Valid @RequestBody final CrmQuoteStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            quoteService.transitionStatus(resolveTenantCode(), resolveActorEmail(), quoteId, request),
            "CRM quote status updated successfully."
        ));
    }

    private String resolveTenantCode() {
        return requestContextResolver.resolveTenantCode(properties);
    }

    private String resolveActorEmail() {
        return requestContextResolver.resolveActorEmail();
    }
}
