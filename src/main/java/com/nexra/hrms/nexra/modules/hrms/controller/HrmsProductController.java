package com.nexra.hrms.nexra.modules.hrms.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.exception.NexraUnauthorizedException;
import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.common.workflow.WorkflowProperties;
import com.nexra.hrms.nexra.common.workflow.WorkflowRuntime;
import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import com.nexra.hrms.nexra.modules.hrms.service.HrmsProductSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/hrms")
@Tag(name = "HRMS Product", description = "HRMS product workflow and module-level summary endpoints.")
public class HrmsProductController {

    private static final Set<String> SUPPORTED_MODULE_KEYS = Set.of(
        "dashboard",
        "employee-core",
        "attendance",
        "leave",
        "timesheet",
        "payroll",
        "expense",
        "onboarding",
        "performance",
        "recruitment"
    );

    private final HrmsProductSummaryService productSummaryService;
    private final WorkflowRuntime workflowRuntime;
    private final WorkflowProperties workflowProperties;

    @GetMapping("/modules/{moduleKey}/summary")
    @Operation(summary = "Get HRMS module summary", description = "Returns tenant-scoped operational summary for a supported HRMS module.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Module summary fetched successfully."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid module key."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required.")
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> moduleSummary(
        @PathVariable @NotBlank @Size(max = 80) final String moduleKey
    ) {
        final String tenantCode = resolveTenantCode();
        validateModuleKey(moduleKey);
        final HrmsProductSummaryService.ModuleSummaryCounts counts = productSummaryService.resolveCounts(tenantCode, moduleKey);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "key", moduleKey,
            "queueCount", counts.queueCount(),
            "pendingApprovals", counts.pendingApprovals(),
            "throughputPercent", counts.throughputPercent()
        ), "HRMS module summary fetched successfully."));
    }

    @PostMapping("/workflow")
    @Operation(summary = "Submit HRMS workflow", description = "Accepts tenant-scoped workflow payload for a supported HRMS module.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> workflow(@RequestBody final HrmsWorkflowRequest request) {
        final String tenantCode = resolveTenantCode();
        validateModuleKey(request.moduleKey());
        final WorkflowRuntime.WorkflowSubmissionResult submission = workflowRuntime.submit(
            tenantCode,
            "HRMS",
            request.moduleKey(),
            workflowProperties.getHrmsSubmitTrigger(),
            request.actorEmail(),
            request.payload()
        );
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "accepted", true,
            "tenantCode", tenantCode,
            "moduleKey", request.moduleKey(),
            "receivedAt", submission.receivedAt(),
            "workflowRef", submission.workflowRef(),
            "status", submission.status()
        ), "HRMS workflow accepted successfully."));
    }

    private String resolveTenantCode() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal) {
            if (!StringUtils.hasText(principal.tenantCode())) {
                throw new NexraUnauthorizedException("Authenticated user is missing tenant context.");
            }
            return principal.tenantCode().trim();
        }
        throw new NexraUnauthorizedException("Authentication is required.");
    }

    private void validateModuleKey(final String moduleKey) {
        if (SUPPORTED_MODULE_KEYS.contains(moduleKey)) {
            return;
        }
        throw new NexraValidationException("Unsupported HRMS module key: " + moduleKey);
    }

    public record HrmsWorkflowRequest(
        @NotBlank @Size(max = 80) String moduleKey,
        @Email @Size(max = 160) String actorEmail,
        Map<String, Object> payload
    ) {
    }
}
