package com.nexra.hrms.nexra.modules.operations.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.common.exception.NexraUnauthorizedException;
import com.nexra.hrms.nexra.common.security.NexraPermission;
import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import com.nexra.hrms.nexra.modules.operations.dto.OpsApprovalCreateRequest;
import com.nexra.hrms.nexra.modules.operations.dto.OpsApprovalDecisionRequest;
import com.nexra.hrms.nexra.modules.operations.model.OpsApprovalRequest;
import com.nexra.hrms.nexra.modules.operations.service.OpsApprovalService;
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

@Tag(name = "Operations Approvals", description = "Approval requests for Operations module.")
@RestController
@RequestMapping("/api/v1/operations/approvals")
@RequiredArgsConstructor
public class OpsApprovalController {

    private final OpsApprovalService approvalService;

    @PostMapping
    @PreAuthorize("hasPermission(null, '" + NexraPermission.OPS_WRITE + "')")
    public ResponseEntity<ApiResponse<OpsApprovalRequest>> create(@Valid @RequestBody final OpsApprovalCreateRequest request) {
        final JwtPrincipal principal = requirePrincipal();
        return ResponseEntity.status(201).body(ApiResponse.created(
            approvalService.create(principal.tenantCode(), principal.email(), request),
            "Operations approval request created successfully."
        ));
    }

    @PostMapping("/{approvalId}/decision")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.OPS_WRITE + "')")
    public ResponseEntity<ApiResponse<OpsApprovalRequest>> decide(
        @PathVariable final String approvalId,
        @Valid @RequestBody final OpsApprovalDecisionRequest request
    ) {
        final JwtPrincipal principal = requirePrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
            approvalService.decide(
                principal.tenantCode(),
                principal.email(),
                principal.userId().toString(),
                principal.roles(),
                approvalId,
                request
            ),
            "Operations approval decision recorded successfully."
        ));
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, '" + NexraPermission.OPS_READ + "')")
    public ResponseEntity<ApiResponse<PageResponse<OpsApprovalRequest>>> list(
        @RequestParam(defaultValue = "0") final int page,
        @RequestParam(defaultValue = "20") final int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            approvalService.list(resolveTenantCode(), page, size),
            "Operations approval requests listed successfully."
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
