package com.nexra.hrms.nexra.modules.hrms.expense.service;

import com.nexra.hrms.nexra.modules.hrms.expense.dto.request.ExpenseCategoryUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.expense.dto.request.ExpenseClaimCreateRequest;
import com.nexra.hrms.nexra.modules.hrms.expense.dto.request.ExpenseDecisionRequest;
import com.nexra.hrms.nexra.modules.hrms.expense.model.ExpenseCategoryView;
import com.nexra.hrms.nexra.modules.hrms.expense.model.ExpenseClaimView;
import com.nexra.hrms.nexra.modules.hrms.expense.security.AuthenticatedExpenseUser;
import java.util.List;
import org.springframework.data.domain.Pageable;
import com.nexra.hrms.nexra.common.api.PageResponse;

/**
 * Defines tenant-scoped expense management operations for categories, claims, approvals, and reimbursements.
 */
public interface ExpenseService {

    ExpenseCategoryView upsertCategory(ExpenseCategoryUpsertRequest request, AuthenticatedExpenseUser actor);

    List<ExpenseCategoryView> listCategories(String tenantCode, boolean includeInactive, AuthenticatedExpenseUser actor);

    ExpenseClaimView createClaim(ExpenseClaimCreateRequest request, AuthenticatedExpenseUser actor);

    List<ExpenseClaimView> listClaims(String tenantCode, String employeeId, String status, AuthenticatedExpenseUser actor);

    /** Paginated expense claims listing. */
    PageResponse<ExpenseClaimView> listClaims(String tenantCode, String employeeId, String status, AuthenticatedExpenseUser actor, Pageable pageable);

    ExpenseClaimView getClaim(String tenantCode, String claimId, AuthenticatedExpenseUser actor);

    ExpenseClaimView approveClaim(String claimId, ExpenseDecisionRequest request, AuthenticatedExpenseUser actor);

    ExpenseClaimView rejectClaim(String claimId, ExpenseDecisionRequest request, AuthenticatedExpenseUser actor);

    ExpenseClaimView markReimbursed(String claimId, String tenantCode, AuthenticatedExpenseUser actor);
}

