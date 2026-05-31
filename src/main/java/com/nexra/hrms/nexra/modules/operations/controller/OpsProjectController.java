package com.nexra.hrms.nexra.modules.operations.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.common.exception.NexraUnauthorizedException;
import com.nexra.hrms.nexra.common.security.NexraPermission;
import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import com.nexra.hrms.nexra.modules.operations.dto.OpsProjectCreateRequest;
import com.nexra.hrms.nexra.modules.operations.model.OpsProject;
import com.nexra.hrms.nexra.modules.operations.service.OpsProjectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Operations Projects", description = "Project management for Operations module.")
@RestController
@RequestMapping("/api/v1/operations/projects")
@RequiredArgsConstructor
public class OpsProjectController {

    private final OpsProjectService projectService;

    @PostMapping
    @PreAuthorize("hasPermission(null, '" + NexraPermission.OPS_WRITE + "')")
    public ResponseEntity<ApiResponse<OpsProject>> create(@Valid @RequestBody final OpsProjectCreateRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created(
            projectService.create(resolveTenantCode(), request),
            "Operations project created successfully."
        ));
    }

    @PostMapping("/from-crm-deal/{dealId}")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.OPS_WRITE + "')")
    public ResponseEntity<ApiResponse<OpsProject>> createFromDeal(@PathVariable final String dealId) {
        final JwtPrincipal principal = requirePrincipal();
        return ResponseEntity.status(201).body(ApiResponse.created(
            projectService.createFromCrmDeal(principal.tenantCode(), dealId, principal.userId().toString()),
            "Operations project created from CRM deal."
        ));
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.OPS_READ + "')")
    public ResponseEntity<ApiResponse<OpsProject>> getById(@PathVariable final String projectId) {
        return ResponseEntity.ok(ApiResponse.ok(
            projectService.findById(resolveTenantCode(), projectId),
            "Operations project fetched successfully."
        ));
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, '" + NexraPermission.OPS_READ + "')")
    public ResponseEntity<ApiResponse<PageResponse<OpsProject>>> list(
        @RequestParam(defaultValue = "0") final int page,
        @RequestParam(defaultValue = "20") final int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            projectService.list(resolveTenantCode(), page, size),
            "Operations projects listed successfully."
        ));
    }

    private String resolveTenantCode() {
        return requirePrincipal().tenantCode();
    }

    private JwtPrincipal requirePrincipal() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal) {
            return principal;
        }
        throw new NexraUnauthorizedException("Authentication is required.");
    }
}
