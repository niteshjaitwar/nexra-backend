package com.nexra.hrms.nexra.modules.crm.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.common.security.NexraPermission;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmLeadConvertRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmLeadCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmLeadUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmLead;
import com.nexra.hrms.nexra.modules.crm.model.CrmLeadConversionResult;
import com.nexra.hrms.nexra.modules.crm.service.CrmLeadService;
import com.nexra.hrms.nexra.modules.crm.support.CrmAccessScope;
import com.nexra.hrms.nexra.modules.crm.support.CrmRequestContextResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for CRM lead management.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Tag(name = "CRM Leads", description = "Lead management APIs for CRM module.")
@RestController
@RequestMapping("/api/v1/crm/leads")
@RequiredArgsConstructor
public class CrmLeadController {

    private final CrmLeadService service;
    private final CrmProperties properties;
    private final CrmRequestContextResolver requestContextResolver;

    /**
     * Creates a CRM lead.
     *
     * @param request lead creation payload.
     * @return created lead envelope.
     */
    @Operation(summary = "Create CRM lead", description = "Creates a new CRM lead.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Lead created."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid payload."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business validation failed.")
    })
    @PostMapping
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_WRITE + "')")
    public ResponseEntity<ApiResponse<CrmLead>> create(@Valid @RequestBody final CrmLeadCreateRequest request) {
        final CrmLead lead = service.create(resolveTenantCode(), request, resolveLeadAccessScope());
        return ResponseEntity.status(201).body(ApiResponse.created(lead, "CRM lead created successfully."));
    }

    /**
     * Retrieves a CRM lead by id.
     *
     * @param leadId lead id.
     * @return lead envelope.
     */
    @Operation(summary = "Get CRM lead", description = "Fetches a CRM lead by id.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lead retrieved."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lead not found.")
    })
    @GetMapping("/{leadId}")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_READ + "')")
    public ResponseEntity<ApiResponse<CrmLead>> getById(@PathVariable final String leadId) {
        return ResponseEntity.ok(ApiResponse.ok(
            service.findById(resolveTenantCode(), leadId, resolveLeadAccessScope()),
            "CRM lead fetched successfully."
        ));
    }

    /**
     * Lists CRM leads using page and size query parameters.
     *
     * @param page page index.
     * @param size page size.
     * @return paged lead envelope.
     */
    @Operation(summary = "List CRM leads", description = "Returns paginated CRM leads.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Leads listed."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Invalid pagination parameters.")
    })
    @GetMapping
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_READ + "')")
    public ResponseEntity<ApiResponse<PageResponse<CrmLead>>> list(
        @RequestParam(defaultValue = "0") final int page,
        @RequestParam(defaultValue = "20") final int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            service.list(resolveTenantCode(), page, size, resolveLeadAccessScope()),
            "CRM leads listed successfully."
        ));
    }

    /**
     * Updates an existing CRM lead.
     *
     * @param leadId lead id.
     * @param request lead update payload.
     * @return updated lead envelope.
     */
    @Operation(summary = "Update CRM lead", description = "Updates an existing CRM lead.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lead updated."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lead not found."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business validation failed.")
    })
    @PutMapping("/{leadId}")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_WRITE + "')")
    public ResponseEntity<ApiResponse<CrmLead>> update(
        @PathVariable final String leadId,
        @Valid @RequestBody final CrmLeadUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            service.update(resolveTenantCode(), leadId, request, resolveLeadAccessScope()),
            "CRM lead updated successfully."
        ));
    }

    @Operation(summary = "Convert CRM lead", description = "Converts lead into account, contact, and deal records.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lead converted."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lead not found."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Lead already converted.")
    })
    @PostMapping("/{leadId}/convert")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_WRITE + "')")
    public ResponseEntity<ApiResponse<CrmLeadConversionResult>> convert(
        @PathVariable final String leadId,
        @Valid @RequestBody final CrmLeadConvertRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.ok(
                service.convertLead(resolveTenantCode(), leadId, request, resolveLeadAccessScope()),
                "CRM lead converted successfully."
            )
        );
    }

    /**
     * Deletes an existing CRM lead.
     *
     * @param leadId lead id.
     * @return empty success envelope.
     */
    @Operation(summary = "Delete CRM lead", description = "Deletes an existing CRM lead.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lead deleted."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lead not found.")
    })
    @DeleteMapping("/{leadId}")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_WRITE + "')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable final String leadId) {
        service.delete(resolveTenantCode(), leadId, resolveLeadAccessScope());
        return ResponseEntity.ok(ApiResponse.empty("CRM lead deleted successfully."));
    }

    private String resolveTenantCode() {
        return requestContextResolver.resolveTenantCode(properties);
    }

    private CrmAccessScope resolveLeadAccessScope() {
        return requestContextResolver.resolveLeadAccessScope(properties);
    }
}
