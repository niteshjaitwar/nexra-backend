package com.nexra.hrms.nexra.modules.crm.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.exception.NexraForbiddenException;
import com.nexra.hrms.nexra.common.exception.NexraUnauthorizedException;
import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.model.CrmLeadStatus;
import com.nexra.hrms.nexra.modules.crm.repository.CrmDealRepository;
import com.nexra.hrms.nexra.modules.crm.repository.CrmLeadRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    private final CrmLeadRepository crmLeadRepository;
    private final CrmDealRepository crmDealRepository;
    private final CrmProperties crmProperties;

    @Operation(summary = "Get CRM pipeline snapshot", description = "Returns tenant-scoped CRM pipeline summary for a supported CRM module.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pipeline snapshot fetched successfully."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid module key."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "CRM product access is missing.")
    })
    @GetMapping("/modules/{moduleKey}/pipeline")
    public ResponseEntity<ApiResponse<Map<String, Object>>> pipelineSnapshot(
        @PathVariable @NotBlank @Size(max = 80) final String moduleKey
    ) {
        final String tenantCode = resolveTenantCode();
        validateModuleKey(moduleKey);
        final long totalLeads = crmLeadRepository.countByTenantCodeIgnoreCase(tenantCode);
        final long wonLeadCount = crmLeadRepository.countByTenantCodeIgnoreCaseAndStatus(tenantCode, CrmLeadStatus.WON);
        final long totalDeals = crmDealRepository.countByTenantCodeIgnoreCase(tenantCode);
        final long wonDeals = crmDealRepository.countByTenantCodeIgnoreCaseAndStageIgnoreCase(tenantCode, "WON");
        final long openDeals = Math.max(0L, totalDeals - wonDeals);
        final long estimatedOpenPipelineValue = openDeals * 125000L;

        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "moduleKey", moduleKey,
            "totalLeads", totalLeads,
            "wonLeadCount", wonLeadCount,
            "totalDeals", totalDeals,
            "wonDeals", wonDeals,
            "openDeals", openDeals,
            "openPipelineValue", estimatedOpenPipelineValue
        ), "CRM pipeline snapshot fetched successfully."));
    }

    private String resolveTenantCode() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal) {
            requireCrmProductScope(principal);
            if (!StringUtils.hasText(principal.tenantCode())) {
                throw new NexraUnauthorizedException("Authenticated CRM user is missing tenant context.");
            }
            return principal.tenantCode().trim();
        }
        if (!crmProperties.isEnforceAuth()) {
            return "nexra";
        }
        throw new NexraUnauthorizedException("Authentication is required.");
    }

    private void requireCrmProductScope(final JwtPrincipal principal) {
        if (principal.products().contains("CRM")) {
            return;
        }
        throw new NexraForbiddenException("User does not have CRM product access.");
    }

    private void validateModuleKey(final String moduleKey) {
        if (SUPPORTED_MODULE_KEYS.contains(moduleKey)) {
            return;
        }
        throw new NexraValidationException("Unsupported CRM module key: " + moduleKey);
    }
}
