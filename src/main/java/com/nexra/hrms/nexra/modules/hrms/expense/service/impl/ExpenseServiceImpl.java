package com.nexra.hrms.nexra.modules.hrms.expense.service.impl;

import com.nexra.hrms.nexra.modules.hrms.expense.dto.request.ExpenseCategoryUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.expense.dto.request.ExpenseClaimCreateRequest;
import com.nexra.hrms.nexra.modules.hrms.expense.dto.request.ExpenseClaimItemRequest;
import com.nexra.hrms.nexra.modules.hrms.expense.dto.request.ExpenseDecisionRequest;
import com.nexra.hrms.nexra.modules.hrms.expense.entity.ExpenseCategoryEntity;
import com.nexra.hrms.nexra.modules.hrms.expense.entity.ExpenseClaimEntity;
import com.nexra.hrms.nexra.modules.hrms.expense.entity.ExpenseClaimItemEntity;
import com.nexra.hrms.nexra.modules.hrms.expense.exception.ExpenseBusinessException;
import com.nexra.hrms.nexra.modules.hrms.expense.exception.ExpenseForbiddenException;
import com.nexra.hrms.nexra.modules.hrms.expense.exception.ExpenseResourceNotFoundException;
import com.nexra.hrms.nexra.modules.hrms.expense.model.ExpenseCategoryView;
import com.nexra.hrms.nexra.modules.hrms.expense.model.ExpenseClaimItemView;
import com.nexra.hrms.nexra.modules.hrms.expense.model.ExpenseClaimView;
import com.nexra.hrms.nexra.modules.hrms.expense.repository.ExpenseCategoryRepository;
import com.nexra.hrms.nexra.modules.hrms.expense.repository.ExpenseClaimItemRepository;
import com.nexra.hrms.nexra.modules.hrms.expense.repository.ExpenseClaimRepository;
import com.nexra.hrms.nexra.modules.hrms.expense.security.AuthenticatedExpenseUser;
import com.nexra.hrms.nexra.modules.hrms.expense.service.ExpenseService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements tenant-scoped expense categories, claims, approvals, and reimbursements.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_REIMBURSED = "REIMBURSED";

    private final ExpenseCategoryRepository categoryRepository;
    private final ExpenseClaimRepository claimRepository;
    private final ExpenseClaimItemRepository claimItemRepository;

    /**
     * Creates or updates an expense category.
     *
     * @param request category payload
     * @param actor authenticated user
     * @return saved category
     */
    @Override
    @Transactional
    public ExpenseCategoryView upsertCategory(final ExpenseCategoryUpsertRequest request, final AuthenticatedExpenseUser actor) {
        verifyTenant(actor, request.tenantCode());
        requireAdmin(actor);
        String tenant = normTenant(request.tenantCode());
        String code = trim(request.code()).toUpperCase();
        log.info("ExpenseServiceImpl - upsertCategory - tenantCode={}, code={}, actor={}", tenant, code, actor.email());

        ExpenseCategoryEntity entity = categoryRepository.findByTenantCodeIgnoreCaseAndCodeIgnoreCase(tenant, code)
            .orElseGet(ExpenseCategoryEntity::new);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID().toString());
            entity.setCreatedBy(actor.email());
        }
        entity.setTenantCode(tenant);
        entity.setCode(code);
        entity.setName(trim(request.name()));
        entity.setMaxAmountPerClaim(request.maxAmountPerClaim() == null ? null : amount(request.maxAmountPerClaim()));
        entity.setRequiresReceipt(request.requiresReceipt());
        entity.setActive(request.active() == null || request.active());
        entity.setUpdatedBy(actor.email());
        return toCategoryModel(categoryRepository.save(entity));
    }

    /**
     * Lists categories for a tenant.
     *
     * @param tenantCode tenant code
     * @param includeInactive include inactive categories
     * @param actor authenticated user
     * @return categories list
     */
    @Override
    public List<ExpenseCategoryView> listCategories(
        final String tenantCode,
        final boolean includeInactive,
        final AuthenticatedExpenseUser actor
    ) {
        verifyTenant(actor, tenantCode);
        log.debug("ExpenseServiceImpl - listCategories - tenantCode={}, includeInactive={}", tenantCode, includeInactive);
        return categoryRepository.findByTenantCodeIgnoreCaseOrderByCodeAsc(normTenant(tenantCode)).stream()
            .filter(category -> includeInactive || category.isActive())
            .map(this::toCategoryModel)
            .toList();
    }

    /**
     * Creates an expense claim with line items after tenant and category validation.
     *
     * @param request claim payload
     * @param actor authenticated user
     * @return created claim
     */
    @Override
    @Transactional
    public ExpenseClaimView createClaim(final ExpenseClaimCreateRequest request, final AuthenticatedExpenseUser actor) {
        verifyTenant(actor, request.tenantCode());
        ensureSelfOrAdmin(actor, request.employeeId());
        String tenant = normTenant(request.tenantCode());
        log.info("ExpenseServiceImpl - createClaim - tenantCode={}, employeeId={}, claimDate={}, itemCount={}",
            tenant, request.employeeId(), request.claimDate(), request.items() == null ? 0 : request.items().size());

        BigDecimal total = BigDecimal.ZERO;
        for (ExpenseClaimItemRequest item : request.items()) {
            ExpenseCategoryEntity category = categoryRepository.findByTenantCodeIgnoreCaseAndCodeIgnoreCase(tenant, item.categoryCode())
                .orElseThrow(() -> new ExpenseResourceNotFoundException("Expense category not found: " + item.categoryCode()));
            if (!category.isActive()) {
                throw new ExpenseBusinessException("Expense category inactive: " + item.categoryCode());
            }
            if (category.isRequiresReceipt() && (item.receiptReference() == null || item.receiptReference().isBlank())) {
                throw new ExpenseBusinessException("Receipt reference required for category: " + category.getCode());
            }
            if (category.getMaxAmountPerClaim() != null && amount(item.amount()).compareTo(category.getMaxAmountPerClaim()) > 0) {
                throw new ExpenseBusinessException("Item amount exceeds category limit for: " + category.getCode());
            }
            total = total.add(amount(item.amount()));
        }
        total = amount(total);

        ExpenseClaimEntity claim = new ExpenseClaimEntity();
        claim.setId(UUID.randomUUID().toString());
        claim.setTenantCode(tenant);
        claim.setEmployeeId(trim(request.employeeId()));
        claim.setClaimDate(request.claimDate());
        claim.setTitle(trim(request.title()));
        claim.setCurrency(trim(request.currency()).toUpperCase());
        claim.setTotalAmount(total);
        claim.setStatus(STATUS_SUBMITTED);
        claim.setCreatedBy(actor.email());
        claim.setUpdatedBy(actor.email());
        ExpenseClaimEntity savedClaim = claimRepository.save(claim);

        for (ExpenseClaimItemRequest item : request.items()) {
            ExpenseClaimItemEntity entity = new ExpenseClaimItemEntity();
            entity.setId(UUID.randomUUID().toString());
            entity.setClaimId(savedClaim.getId());
            entity.setTenantCode(tenant);
            entity.setExpenseDate(item.expenseDate());
            entity.setCategoryCode(trim(item.categoryCode()).toUpperCase());
            entity.setDescriptionText(trim(item.description()));
            entity.setAmount(amount(item.amount()));
            entity.setReceiptReference(blankToNull(item.receiptReference()));
            entity.setCreatedBy(actor.email());
            entity.setUpdatedBy(actor.email());
            claimItemRepository.save(entity);
        }
        return toClaimModel(savedClaim);
    }

    /**
     * Lists claims for a tenant with optional employee/status filters.
     *
     * @param tenantCode tenant code
     * @param employeeId optional employee id
     * @param status optional status
     * @param actor authenticated user
     * @return claims list
     */
    @Override
    public List<ExpenseClaimView> listClaims(
        final String tenantCode,
        final String employeeId,
        final String status,
        final AuthenticatedExpenseUser actor
    ) {
        verifyTenant(actor, tenantCode);
        String tenant = normTenant(tenantCode);
        String employeeFilter = blankToNull(employeeId);
        if (employeeFilter != null) {
            ensureSelfOrAdmin(actor, employeeFilter);
        }
        List<ExpenseClaimEntity> claims = employeeFilter != null
            ? claimRepository.findByTenantCodeIgnoreCaseAndEmployeeIdOrderByCreatedAtDesc(tenant, employeeFilter)
            : (status != null && !status.isBlank()
                ? claimRepository.findByTenantCodeIgnoreCaseAndStatusIgnoreCaseOrderByCreatedAtDesc(tenant, status)
                : claimRepository.findByTenantCodeIgnoreCaseOrderByCreatedAtDesc(tenant));
        String statusFilter = blankToNullUpper(status);
        return claims.stream()
            .filter(claim -> statusFilter == null || claim.getStatus().equalsIgnoreCase(statusFilter))
            .map(this::toClaimModel)
            .toList();
    }

    @Override
    public com.nexra.hrms.nexra.common.api.PageResponse<ExpenseClaimView> listClaims(
        final String tenantCode,
        final String employeeId,
        final String status,
        final AuthenticatedExpenseUser actor,
        final org.springframework.data.domain.Pageable pageable
    ) {
        verifyTenant(actor, tenantCode);
        String tenant = normTenant(tenantCode);
        String employeeFilter = blankToNull(employeeId);
        if (employeeFilter != null) {
            ensureSelfOrAdmin(actor, employeeFilter);
        }
        String statusFilter = blankToNullUpper(status);
        org.springframework.data.domain.Page<ExpenseClaimEntity> page;
        if (employeeFilter == null && statusFilter == null) {
            page = claimRepository.findByTenantCodeIgnoreCase(tenant, pageable);
        } else if (employeeFilter != null && statusFilter == null) {
            page = claimRepository.findByTenantCodeIgnoreCaseAndEmployeeId(tenant, employeeFilter, pageable);
        } else if (employeeFilter == null) {
            page = claimRepository.findByTenantCodeIgnoreCaseAndStatusIgnoreCase(tenant, statusFilter, pageable);
        } else {
            page = claimRepository.findByTenantCodeIgnoreCaseAndEmployeeIdAndStatusIgnoreCase(tenant, employeeFilter, statusFilter, pageable);
        }
        return com.nexra.hrms.nexra.common.api.PageResponse.map(
            com.nexra.hrms.nexra.common.api.PageResponse.from(page), this::toClaimModel
        );
    }

    /**
     * Returns a claim by id.
     *
     * @param tenantCode tenant code
     * @param claimId claim id
     * @param actor authenticated user
     * @return claim view
     */
    @Override
    public ExpenseClaimView getClaim(final String tenantCode, final String claimId, final AuthenticatedExpenseUser actor) {
        verifyTenant(actor, tenantCode);
        ExpenseClaimEntity claim = claimRepository.findByIdAndTenantCodeIgnoreCase(claimId, normTenant(tenantCode))
            .orElseThrow(() -> new ExpenseResourceNotFoundException("Expense claim not found: " + claimId));
        ensureSelfOrAdmin(actor, claim.getEmployeeId());
        return toClaimModel(claim);
    }

    /**
     * Approves a submitted claim.
     *
     * @param claimId claim id
     * @param request decision request
     * @param actor authenticated user
     * @return updated claim
     */
    @Override
    @Transactional
    public ExpenseClaimView approveClaim(final String claimId, final ExpenseDecisionRequest request, final AuthenticatedExpenseUser actor) {
        return decide(claimId, request, actor, true);
    }

    /**
     * Rejects a submitted claim.
     *
     * @param claimId claim id
     * @param request decision request
     * @param actor authenticated user
     * @return updated claim
     */
    @Override
    @Transactional
    public ExpenseClaimView rejectClaim(final String claimId, final ExpenseDecisionRequest request, final AuthenticatedExpenseUser actor) {
        return decide(claimId, request, actor, false);
    }

    /**
     * Marks an approved claim as reimbursed.
     *
     * @param claimId claim id
     * @param tenantCode tenant code
     * @param actor authenticated user
     * @return updated claim
     */
    @Override
    @Transactional
    public ExpenseClaimView markReimbursed(final String claimId, final String tenantCode, final AuthenticatedExpenseUser actor) {
        verifyTenant(actor, tenantCode);
        requireAdmin(actor);
        ExpenseClaimEntity claim = claimRepository.findByIdAndTenantCodeIgnoreCase(claimId, normTenant(tenantCode))
            .orElseThrow(() -> new ExpenseResourceNotFoundException("Expense claim not found: " + claimId));
        if (!STATUS_APPROVED.equalsIgnoreCase(claim.getStatus())) {
            throw new ExpenseBusinessException("Only approved claims can be reimbursed");
        }
        claim.setStatus(STATUS_REIMBURSED);
        claim.setReimbursedAt(Instant.now());
        claim.setUpdatedBy(actor.email());
        log.info("ExpenseServiceImpl - markReimbursed - tenantCode={}, claimId={}, actor={}", tenantCode, claimId, actor.email());
        return toClaimModel(claimRepository.save(claim));
    }

    private ExpenseClaimView decide(
        final String claimId,
        final ExpenseDecisionRequest request,
        final AuthenticatedExpenseUser actor,
        final boolean approve
    ) {
        verifyTenant(actor, request.tenantCode());
        requireAdmin(actor);
        ExpenseClaimEntity claim = claimRepository.findByIdAndTenantCodeIgnoreCase(claimId, normTenant(request.tenantCode()))
            .orElseThrow(() -> new ExpenseResourceNotFoundException("Expense claim not found: " + claimId));
        if (!STATUS_SUBMITTED.equalsIgnoreCase(claim.getStatus())) {
            throw new ExpenseBusinessException("Expense claim already decided: " + claim.getStatus());
        }
        claim.setStatus(approve ? STATUS_APPROVED : STATUS_REJECTED);
        claim.setApproverUserId(actor.userId().toString());
        claim.setApproverEmail(actor.email());
        claim.setApprovalComment(blankToNull(request.comment()));
        claim.setUpdatedBy(actor.email());
        log.info("ExpenseServiceImpl - decide - tenantCode={}, claimId={}, action={}, approver={}",
            request.tenantCode(), claimId, approve ? "APPROVE" : "REJECT", actor.email());
        return toClaimModel(claimRepository.save(claim));
    }

    private ExpenseCategoryView toCategoryModel(final ExpenseCategoryEntity entity) {
        return new ExpenseCategoryView(
            entity.getId(),
            entity.getTenantCode(),
            entity.getCode(),
            entity.getName(),
            entity.getMaxAmountPerClaim(),
            entity.isRequiresReceipt(),
            entity.isActive(),
            entity.getUpdatedAt(),
            entity.getUpdatedBy()
        );
    }

    private ExpenseClaimView toClaimModel(final ExpenseClaimEntity claim) {
        List<ExpenseClaimItemView> items = claimItemRepository.findByClaimIdOrderByExpenseDateAsc(claim.getId()).stream()
            .map(this::toClaimItemModel)
            .toList();
        return new ExpenseClaimView(
            claim.getId(),
            claim.getTenantCode(),
            claim.getEmployeeId(),
            claim.getClaimDate(),
            claim.getTitle(),
            claim.getCurrency(),
            claim.getTotalAmount(),
            claim.getStatus(),
            claim.getApproverUserId(),
            claim.getApproverEmail(),
            claim.getApprovalComment(),
            claim.getReimbursedAt(),
            claim.getCreatedAt(),
            claim.getUpdatedAt(),
            items
        );
    }

    private ExpenseClaimItemView toClaimItemModel(final ExpenseClaimItemEntity item) {
        return new ExpenseClaimItemView(
            item.getId(),
            item.getExpenseDate(),
            item.getCategoryCode(),
            item.getDescriptionText(),
            item.getAmount(),
            item.getReceiptReference()
        );
    }

    private void ensureSelfOrAdmin(final AuthenticatedExpenseUser actor, final String employeeId) {
        if (actor.userId().toString().equalsIgnoreCase(trim(employeeId)) || isAdmin(actor)) {
            return;
        }
        throw new ExpenseForbiddenException("User cannot access expense claims for another employee");
    }

    private void requireAdmin(final AuthenticatedExpenseUser actor) {
        if (!isAdmin(actor)) {
            throw new ExpenseForbiddenException("User does not have expense administration permission");
        }
    }

    private boolean isAdmin(final AuthenticatedExpenseUser actor) {
        return hasRole(actor, "PLATFORM_ADMIN")
            || hasRole(actor, "TENANT_ADMIN")
            || hasRole(actor, "HR_ADMIN")
            || hasRole(actor, "FINANCE_ADMIN");
    }

    private boolean hasRole(final AuthenticatedExpenseUser actor, final String role) {
        return actor.roles().contains(role) || actor.roles().contains("ROLE_" + role);
    }

    private void verifyTenant(final AuthenticatedExpenseUser actor, final String tenantCode) {
        if (!actor.tenantCode().equalsIgnoreCase(tenantCode)) {
            throw new ExpenseBusinessException("Token tenant does not match requested tenant");
        }
    }

    private BigDecimal amount(final BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private String normTenant(final String value) {
        return trim(value).toUpperCase();
    }

    private String trim(final String value) {
        return value == null ? null : value.trim();
    }

    private String blankToNull(final String value) {
        String trimmed = trim(value);
        return trimmed == null || trimmed.isBlank() ? null : trimmed;
    }

    private String blankToNullUpper(final String value) {
        String trimmed = blankToNull(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }
}

