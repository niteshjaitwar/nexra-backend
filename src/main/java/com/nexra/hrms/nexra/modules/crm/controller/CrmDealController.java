package com.nexra.hrms.nexra.modules.crm.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmDealCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmDealUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmDeal;
import com.nexra.hrms.nexra.modules.crm.service.CrmDealService;
import com.nexra.hrms.nexra.modules.crm.support.CrmRequestContextResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "CRM Deals", description = "Deal management APIs for CRM module.")
@RestController
@RequestMapping("/api/v1/crm/deals")
@RequiredArgsConstructor
public class CrmDealController {

    private final CrmDealService service;
    private final CrmProperties properties;
    private final CrmRequestContextResolver requestContextResolver;

    @Operation(summary = "Create CRM deal")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "CRM deal created."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid deal payload."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "CRM product access missing.")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CrmDeal>> create(@Valid @RequestBody final CrmDealCreateRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created(
            service.create(resolveTenantCode(), request, requestContextResolver.resolveCrmAccessScope(properties)),
            "CRM deal created successfully."
        ));
    }

    @Operation(summary = "Get CRM deal")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "CRM deal fetched."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "CRM product access missing."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "CRM deal not found.")
    })
    @GetMapping("/{dealId}")
    public ResponseEntity<ApiResponse<CrmDeal>> getById(@PathVariable final String dealId) {
        return ResponseEntity.ok(ApiResponse.ok(
            service.findById(resolveTenantCode(), dealId, requestContextResolver.resolveCrmAccessScope(properties)),
            "CRM deal fetched successfully."
        ));
    }

    @Operation(summary = "List CRM deals")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "CRM deals listed."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "CRM product access missing."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Invalid pagination parameters.")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CrmDeal>>> list(
        @RequestParam(defaultValue = "0") final int page,
        @RequestParam(defaultValue = "20") final int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            service.list(resolveTenantCode(), page, size, requestContextResolver.resolveCrmAccessScope(properties)),
            "CRM deals listed successfully."
        ));
    }

    @Operation(summary = "Update CRM deal")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "CRM deal updated."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid deal payload."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "CRM product access missing."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "CRM deal not found.")
    })
    @PutMapping("/{dealId}")
    public ResponseEntity<ApiResponse<CrmDeal>> update(
        @PathVariable final String dealId,
        @Valid @RequestBody final CrmDealUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            service.update(resolveTenantCode(), dealId, request, requestContextResolver.resolveCrmAccessScope(properties)),
            "CRM deal updated successfully."
        ));
    }

    @Operation(summary = "Delete CRM deal")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "CRM deal deleted."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "CRM product access missing."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "CRM deal not found.")
    })
    @DeleteMapping("/{dealId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable final String dealId) {
        service.delete(resolveTenantCode(), dealId, requestContextResolver.resolveCrmAccessScope(properties));
        return ResponseEntity.ok(ApiResponse.empty("CRM deal deleted successfully."));
    }

    private String resolveTenantCode() {
        return requestContextResolver.resolveTenantCode(properties);
    }
}

