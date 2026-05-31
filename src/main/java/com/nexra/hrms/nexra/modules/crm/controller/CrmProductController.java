package com.nexra.hrms.nexra.modules.crm.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.common.security.NexraPermission;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.service.CrmPipelineMetricsService;
import com.nexra.hrms.nexra.modules.crm.support.CrmRequestContextResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/crm")
@Tag(name = "CRM Product", description = "CRM pipeline and product-level summary endpoints.")
public class CrmProductController {

    private static final Set<String> SUPPORTED_MODULE_KEYS = Set.of(
        "crm-dashboard",
        "crm-leads",
        "crm-contacts",
        "crm-deals",
        "crm-activities",
        "campaigns-marketing"
    );

    private final CrmPipelineMetricsService pipelineMetricsService;
    private final CrmProperties crmProperties;
    private final CrmRequestContextResolver requestContextResolver;

    @Operation(summary = "Get CRM pipeline snapshot", description = "Returns tenant-scoped CRM pipeline summary for a supported CRM module.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pipeline snapshot fetched successfully."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid module key."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "CRM product access is missing.")
    })
    @GetMapping("/modules/{moduleKey}/pipeline")
    @PreAuthorize("hasPermission(null, '" + NexraPermission.CRM_READ + "')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> pipelineSnapshot(
        @PathVariable @NotBlank @Size(max = 80) final String moduleKey
    ) {
        final String tenantCode = resolveTenantCode();
        validateModuleKey(moduleKey);
        return ResponseEntity.ok(ApiResponse.ok(
            pipelineMetricsService.snapshot(tenantCode, moduleKey),
            "CRM pipeline snapshot fetched successfully."
        ));
    }

    private String resolveTenantCode() {
        return requestContextResolver.resolveTenantCode(crmProperties);
    }

    private void validateModuleKey(final String moduleKey) {
        if (SUPPORTED_MODULE_KEYS.contains(moduleKey)) {
            return;
        }
        throw new NexraValidationException("Unsupported CRM module key: " + moduleKey);
    }
}
