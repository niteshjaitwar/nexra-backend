package com.nexra.hrms.nexra.modules.crm.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmActivityCreateRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmActivity;
import com.nexra.hrms.nexra.modules.crm.service.CrmActivityService;
import com.nexra.hrms.nexra.modules.crm.support.CrmRequestContextResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "CRM Activities", description = "Activity timeline APIs for leads, contacts, and deals.")
@RestController
@RequestMapping("/api/v1/crm/activities")
@RequiredArgsConstructor
public class CrmActivityController {

    private final CrmActivityService service;
    private final CrmProperties properties;
    private final CrmRequestContextResolver requestContextResolver;

    @Operation(summary = "Create CRM activity", description = "Creates a timeline activity linked to one CRM record.")
    @PostMapping
    public ResponseEntity<ApiResponse<CrmActivity>> create(@Valid @RequestBody final CrmActivityCreateRequest request) {
        return ResponseEntity.status(201)
            .body(ApiResponse.created(
                service.create(resolveTenantCode(), request, requestContextResolver.resolveCrmAccessScope(properties)),
                "CRM activity created successfully."
            ));
    }

    @Operation(summary = "List CRM activities", description = "Returns CRM timeline activities, optionally filtered by one record id.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CrmActivity>>> list(
        @RequestParam(required = false) final String leadId,
        @RequestParam(required = false) final String contactId,
        @RequestParam(required = false) final String dealId,
        @RequestParam(defaultValue = "0") final int page,
        @RequestParam(defaultValue = "20") final int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            service.list(
                resolveTenantCode(),
                leadId,
                contactId,
                dealId,
                page,
                size,
                requestContextResolver.resolveCrmAccessScope(properties)
            ),
            "CRM activities listed successfully."
        ));
    }

    private String resolveTenantCode() {
        return requestContextResolver.resolveTenantCode(properties);
    }
}
