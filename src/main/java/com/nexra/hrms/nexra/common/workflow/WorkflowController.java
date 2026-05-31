package com.nexra.hrms.nexra.common.workflow;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.common.exception.NexraUnauthorizedException;
import com.nexra.hrms.nexra.common.security.NexraPermission;
import com.nexra.hrms.nexra.common.workflow.dto.WorkflowAdvanceRequest;
import com.nexra.hrms.nexra.common.workflow.model.WorkflowInstance;
import com.nexra.hrms.nexra.common.workflow.model.WorkflowStepHistory;
import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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
import jakarta.validation.Valid;

@Tag(name = "Workflow", description = "Tenant-scoped workflow instance and step-history read APIs.")
@RestController
@RequestMapping("/api/v1/workflow/instances")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowQueryService queryService;
    private final WorkflowRuntime workflowRuntime;

    @Operation(summary = "List workflow instances for the authenticated tenant")
    @GetMapping
    @PreAuthorize("hasPermission(null, '" + NexraPermission.WORKFLOW_READ + "')")
    public ResponseEntity<ApiResponse<PageResponse<WorkflowInstance>>> list(
        @RequestParam(defaultValue = "0") final int page,
        @RequestParam(defaultValue = "20") final int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            queryService.listInstances(resolveTenantCode(), page, size),
            "Workflow instances listed successfully."
        ));
    }

    @Operation(summary = "Get workflow instance by id")
    @GetMapping("/{instanceId}")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.WORKFLOW_READ + "')")
    public ResponseEntity<ApiResponse<WorkflowInstance>> getById(@PathVariable final String instanceId) {
        return ResponseEntity.ok(ApiResponse.ok(
            queryService.getInstance(resolveTenantCode(), instanceId),
            "Workflow instance fetched successfully."
        ));
    }

    @Operation(summary = "Get workflow step history")
    @GetMapping("/{instanceId}/history")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.WORKFLOW_READ + "')")
    public ResponseEntity<ApiResponse<List<WorkflowStepHistory>>> history(@PathVariable final String instanceId) {
        return ResponseEntity.ok(ApiResponse.ok(
            queryService.getStepHistory(resolveTenantCode(), instanceId),
            "Workflow step history fetched successfully."
        ));
    }

    @Operation(summary = "Advance or reject the current workflow step")
    @PostMapping("/{instanceId}/advance")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.WORKFLOW_WRITE + "')")
    public ResponseEntity<ApiResponse<WorkflowInstance>> advance(
        @PathVariable final String instanceId,
        @Valid @RequestBody final WorkflowAdvanceRequest request
    ) {
        final JwtPrincipal principal = requirePrincipal();
        workflowRuntime.advance(
            principal.tenantCode(),
            instanceId,
            Boolean.TRUE.equals(request.approve()),
            principal.email(),
            request.notes()
        );
        return ResponseEntity.ok(ApiResponse.ok(
            queryService.getInstance(principal.tenantCode(), instanceId),
            "Workflow instance advanced successfully."
        ));
    }

    private JwtPrincipal requirePrincipal() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal) {
            if (principal.tenantCode() == null || principal.tenantCode().isBlank()) {
                throw new NexraUnauthorizedException("Authenticated user is missing tenant context.");
            }
            return principal;
        }
        throw new NexraUnauthorizedException("Authentication is required.");
    }

    private String resolveTenantCode() {
        return requirePrincipal().tenantCode();
    }
}
