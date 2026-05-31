package com.nexra.hrms.nexra.modules.operations.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.common.exception.NexraUnauthorizedException;
import com.nexra.hrms.nexra.common.security.NexraPermission;
import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import com.nexra.hrms.nexra.modules.operations.dto.OpsTaskCreateRequest;
import com.nexra.hrms.nexra.modules.operations.dto.OpsTaskStatusUpdateRequest;
import com.nexra.hrms.nexra.modules.operations.model.OpsTask;
import com.nexra.hrms.nexra.modules.operations.service.OpsTaskService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Operations Tasks", description = "Task management for Operations module.")
@RestController
@RequestMapping("/api/v1/operations/tasks")
@RequiredArgsConstructor
public class OpsTaskController {

    private final OpsTaskService taskService;

    @PostMapping
    @PreAuthorize("hasPermission(null, '" + NexraPermission.OPS_WRITE + "')")
    public ResponseEntity<ApiResponse<OpsTask>> create(@Valid @RequestBody final OpsTaskCreateRequest request) {
        final JwtPrincipal principal = requirePrincipal();
        return ResponseEntity.status(201).body(ApiResponse.created(
            taskService.create(principal.tenantCode(), principal.email(), request),
            "Operations task created successfully."
        ));
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, '" + NexraPermission.OPS_READ + "')")
    public ResponseEntity<ApiResponse<PageResponse<OpsTask>>> list(
        @RequestParam final String projectId,
        @RequestParam(defaultValue = "0") final int page,
        @RequestParam(defaultValue = "20") final int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            taskService.listByProject(resolveTenantCode(), projectId, page, size),
            "Operations tasks listed successfully."
        ));
    }

    @PutMapping("/{taskId}/status")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.OPS_WRITE + "')")
    public ResponseEntity<ApiResponse<OpsTask>> updateStatus(
        @PathVariable final String taskId,
        @Valid @RequestBody final OpsTaskStatusUpdateRequest request
    ) {
        final JwtPrincipal principal = requirePrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
            taskService.updateStatus(principal.tenantCode(), principal.email(), taskId, request),
            "Operations task status updated successfully."
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
