package com.nexra.hrms.nexra.modules.hrms.expense.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.modules.hrms.expense.dto.request.ExpenseCategoryUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.expense.dto.request.ExpenseClaimCreateRequest;
import com.nexra.hrms.nexra.modules.hrms.expense.dto.request.ExpenseDecisionRequest;
import com.nexra.hrms.nexra.modules.hrms.expense.exception.ExpenseUnauthorizedException;
import com.nexra.hrms.nexra.modules.hrms.expense.model.ExpenseCategoryView;
import com.nexra.hrms.nexra.modules.hrms.expense.model.ExpenseClaimView;
import com.nexra.hrms.nexra.modules.hrms.expense.security.AuthenticatedExpenseUser;
import com.nexra.hrms.nexra.modules.hrms.expense.security.ExpenseAuthFilter;
import com.nexra.hrms.nexra.modules.hrms.expense.service.ExpenseService;
import com.nexra.hrms.nexra.modules.hrms.employee.validation.TenantCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

/**
 * Exposes tenant-scoped expense APIs for categories, claims, approvals, and reimbursements.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/expense")
@Slf4j
@Validated
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * Returns service availability metadata.
     *
     * @return status payload
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        return ResponseEntity.ok(ApiResponse.success("expense service is available.", Map.of(
            "service", "expense",
            "timestamp", Instant.now().toString(),
            "state", "UP"
        )));
    }

    /**
     * Returns capabilities metadata.
     *
     * @return capabilities payload
     */
    @GetMapping("/capabilities")
    public ResponseEntity<ApiResponse<Map<String, Object>>> capabilities() {
        return ResponseEntity.ok(ApiResponse.success("expense capabilities fetched successfully.", Map.of(
            "domains", List.of("categories", "claims", "approvals", "reimbursements"),
            "storage", "MySQL + Flyway",
            "auth", "JWT tenant-scoped"
        )));
    }

    /**
     * Creates or updates an expense category.
     *
     * @param request category payload
     * @param httpRequest servlet request
     * @return category response
     */
    @PutMapping("/categories")
    public ResponseEntity<ApiResponse<ExpenseCategoryView>> upsertCategory(
        @Valid @RequestBody final ExpenseCategoryUpsertRequest request,
        final HttpServletRequest httpRequest
    ) {
        log.info("ExpenseController - upsertCategory - tenantCode={}, code={}", request.tenantCode(), request.code());
        return ResponseEntity.ok(ApiResponse.success(
            "Expense category saved successfully.",
            expenseService.upsertCategory(request, currentUser(httpRequest))
        ));
    }

    /**
     * Lists categories for a tenant.
     *
     * @param tenantCode tenant code
     * @param includeInactive include inactive categories when true
     * @param httpRequest servlet request
     * @return categories response
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<ExpenseCategoryView>>> listCategories(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        @RequestParam(defaultValue = "false") final boolean includeInactive,
        final HttpServletRequest httpRequest
    ) {
        log.debug("ExpenseController - listCategories - tenantCode={}, includeInactive={}", tenantCode, includeInactive);
        return ResponseEntity.ok(ApiResponse.success(
            "Expense categories fetched successfully.",
            expenseService.listCategories(tenantCode, includeInactive, currentUser(httpRequest))
        ));
    }

    /**
     * Creates a new expense claim.
     *
     * @param request claim payload
     * @param httpRequest servlet request
     * @return claim response
     */
    @PostMapping("/claims")
    public ResponseEntity<ApiResponse<ExpenseClaimView>> createClaim(
        @Valid @RequestBody final ExpenseClaimCreateRequest request,
        final HttpServletRequest httpRequest
    ) {
        log.info("ExpenseController - createClaim - tenantCode={}, employeeId={}, itemCount={}",
            request.tenantCode(), request.employeeId(), request.items() == null ? 0 : request.items().size());
        return ResponseEntity.ok(ApiResponse.success(
            "Expense claim created successfully.",
            expenseService.createClaim(request, currentUser(httpRequest))
        ));
    }

    /**
     * Lists expense claims for a tenant with optional filters.
     *
     * @param tenantCode tenant code
     * @param employeeId optional employee id
     * @param status optional claim status
     * @param httpRequest servlet request
     * @return claims response
     */
    @GetMapping("/claims")
    public ResponseEntity<ApiResponse<com.nexra.hrms.nexra.common.api.PageResponse<ExpenseClaimView>>> listClaims(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        @RequestParam(required = false) final String employeeId,
        @RequestParam(required = false) final String status,
        @RequestParam(defaultValue = "0") @Min(0) final int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) final int size,
        final HttpServletRequest httpRequest
    ) {
        log.debug("ExpenseController - listClaims - tenantCode={}, employeeId={}, status={}", tenantCode, employeeId, status);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
            page, Math.min(size, 100), org.springframework.data.domain.Sort.by("createdAt").descending()
        );
        return ResponseEntity.ok(ApiResponse.success(
            "Expense claims fetched successfully.",
            expenseService.listClaims(tenantCode, employeeId, status, currentUser(httpRequest), pageable)
        ));
    }

    /**
     * Returns a single expense claim.
     *
     * @param claimId claim id
     * @param tenantCode tenant code
     * @param httpRequest servlet request
     * @return claim response
     */
    @GetMapping("/claims/{claimId}")
    public ResponseEntity<ApiResponse<ExpenseClaimView>> getClaim(
        @PathVariable final String claimId,
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Expense claim fetched successfully.",
            expenseService.getClaim(tenantCode, claimId, currentUser(httpRequest))
        ));
    }

    /**
     * Approves a submitted expense claim.
     *
     * @param claimId claim id
     * @param request approval payload
     * @param httpRequest servlet request
     * @return claim response
     */
    @PostMapping("/claims/{claimId}/approve")
    public ResponseEntity<ApiResponse<ExpenseClaimView>> approve(
        @PathVariable final String claimId,
        @Valid @RequestBody final ExpenseDecisionRequest request,
        final HttpServletRequest httpRequest
    ) {
        log.info("ExpenseController - approve - claimId={}, tenantCode={}", claimId, request.tenantCode());
        return ResponseEntity.ok(ApiResponse.success(
            "Expense claim approved successfully.",
            expenseService.approveClaim(claimId, request, currentUser(httpRequest))
        ));
    }

    /**
     * Rejects a submitted expense claim.
     *
     * @param claimId claim id
     * @param request rejection payload
     * @param httpRequest servlet request
     * @return claim response
     */
    @PostMapping("/claims/{claimId}/reject")
    public ResponseEntity<ApiResponse<ExpenseClaimView>> reject(
        @PathVariable final String claimId,
        @Valid @RequestBody final ExpenseDecisionRequest request,
        final HttpServletRequest httpRequest
    ) {
        log.info("ExpenseController - reject - claimId={}, tenantCode={}", claimId, request.tenantCode());
        return ResponseEntity.ok(ApiResponse.success(
            "Expense claim rejected successfully.",
            expenseService.rejectClaim(claimId, request, currentUser(httpRequest))
        ));
    }

    /**
     * Marks an approved claim as reimbursed.
     *
     * @param claimId claim id
     * @param tenantCode tenant code
     * @param httpRequest servlet request
     * @return claim response
     */
    @PostMapping("/claims/{claimId}/reimburse")
    public ResponseEntity<ApiResponse<ExpenseClaimView>> reimburse(
        @PathVariable final String claimId,
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        final HttpServletRequest httpRequest
    ) {
        log.info("ExpenseController - reimburse - claimId={}, tenantCode={}", claimId, tenantCode);
        return ResponseEntity.ok(ApiResponse.success(
            "Expense claim marked reimbursed successfully.",
            expenseService.markReimbursed(claimId, tenantCode, currentUser(httpRequest))
        ));
    }

    private AuthenticatedExpenseUser currentUser(final HttpServletRequest request) {
        Object value = request.getAttribute(ExpenseAuthFilter.ATTR_AUTH_USER);
        if (value instanceof AuthenticatedExpenseUser user) {
            return user;
        }
        throw new ExpenseUnauthorizedException("Missing authenticated expense user");
    }
}

