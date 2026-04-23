package com.nexra.hrms.nexra.modules.crm.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.exception.NexraForbiddenException;
import com.nexra.hrms.nexra.common.exception.NexraUnauthorizedException;
import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.model.CrmLeadStatus;
import com.nexra.hrms.nexra.modules.crm.repository.CrmLeadRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
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
@RequestMapping("/api/crm")
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
    private final CrmProperties crmProperties;

    @GetMapping("/modules/{moduleKey}/pipeline")
    public ResponseEntity<ApiResponse<Map<String, Object>>> pipelineSnapshot(
        @PathVariable @NotBlank @Size(max = 80) final String moduleKey
    ) {
        final String tenantCode = resolveTenantCode();
        validateModuleKey(moduleKey);
        final long totalRecords = crmLeadRepository.countByTenantCodeIgnoreCase(tenantCode);
        final long wonCount = crmLeadRepository.countByTenantCodeIgnoreCaseAndStatus(tenantCode, CrmLeadStatus.WON);
        final long openLeadCount = totalRecords - wonCount;
        final long openPipelineValue = openLeadCount * 125000L;

        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "moduleKey", moduleKey,
            "totalRecords", totalRecords,
            "openPipelineValue", openPipelineValue,
            "wonCount", wonCount
        ), "CRM pipeline snapshot fetched successfully."));
    }

    @PostMapping("/records/mutate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> mutateRecord(@RequestBody final CrmRecordMutationRequest request) {
        final String tenantCode = resolveTenantCode();
        validateModuleKey(request.moduleKey());
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "accepted", true,
            "tenantCode", tenantCode,
            "moduleKey", request.moduleKey(),
            "receivedAt", Instant.now().toString(),
            "mutationRef", "crm-" + System.nanoTime()
        ), "CRM mutation accepted successfully."));
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

    public record CrmRecordMutationRequest(
        @NotBlank @Size(max = 80) String moduleKey,
        @Email @Size(max = 160) String ownerEmail,
        Map<String, Object> payload
    ) {
    }
}
