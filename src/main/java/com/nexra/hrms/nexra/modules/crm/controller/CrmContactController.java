package com.nexra.hrms.nexra.modules.crm.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmContactCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmContactUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmContact;
import com.nexra.hrms.nexra.modules.crm.service.CrmContactService;
import com.nexra.hrms.nexra.modules.crm.support.CrmAccessScope;
import com.nexra.hrms.nexra.modules.crm.support.CrmRequestContextResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.nexra.hrms.nexra.common.security.NexraPermission;
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

@Tag(name = "CRM Contacts", description = "Contact management APIs for CRM module.")
@RestController
@RequestMapping("/api/v1/crm/contacts")
@RequiredArgsConstructor
public class CrmContactController {

    private final CrmContactService service;
    private final CrmProperties properties;
    private final CrmRequestContextResolver requestContextResolver;

    @Operation(summary = "Create CRM contact")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "CRM contact created."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid contact payload."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "CRM product access missing.")
    })
    @PostMapping
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_WRITE + "')")
    public ResponseEntity<ApiResponse<CrmContact>> create(@Valid @RequestBody final CrmContactCreateRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created(
            service.create(resolveTenantCode(), request, resolveCrmAccessScope()),
            "CRM contact created successfully."
        ));
    }

    @Operation(summary = "Get CRM contact")
    @GetMapping("/{contactId}")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_READ + "')")
    public ResponseEntity<ApiResponse<CrmContact>> getById(@PathVariable final String contactId) {
        return ResponseEntity.ok(ApiResponse.ok(
            service.findById(resolveTenantCode(), contactId, resolveCrmAccessScope()),
            "CRM contact fetched successfully."
        ));
    }

    @Operation(summary = "List CRM contacts")
    @GetMapping
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_READ + "')")
    public ResponseEntity<ApiResponse<PageResponse<CrmContact>>> list(
        @RequestParam(defaultValue = "0") final int page,
        @RequestParam(defaultValue = "20") final int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            service.list(resolveTenantCode(), page, size, resolveCrmAccessScope()),
            "CRM contacts listed successfully."
        ));
    }

    @Operation(summary = "Update CRM contact")
    @PutMapping("/{contactId}")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_WRITE + "')")
    public ResponseEntity<ApiResponse<CrmContact>> update(
        @PathVariable final String contactId,
        @Valid @RequestBody final CrmContactUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            service.update(resolveTenantCode(), contactId, request, resolveCrmAccessScope()),
            "CRM contact updated successfully."
        ));
    }

    @Operation(summary = "Delete CRM contact")
    @DeleteMapping("/{contactId}")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_WRITE + "')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable final String contactId) {
        service.delete(resolveTenantCode(), contactId, resolveCrmAccessScope());
        return ResponseEntity.ok(ApiResponse.empty("CRM contact deleted successfully."));
    }

    private String resolveTenantCode() {
        return requestContextResolver.resolveTenantCode(properties);
    }

    private CrmAccessScope resolveCrmAccessScope() {
        return requestContextResolver.resolveCrmAccessScope(properties);
    }
}
