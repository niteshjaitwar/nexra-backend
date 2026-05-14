package com.nexra.hrms.nexra.modules.hrms.performance.service;

import com.nexra.hrms.nexra.modules.hrms.performance.dto.request.GoalUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.performance.dto.request.ReviewCompleteRequest;
import com.nexra.hrms.nexra.modules.hrms.performance.dto.request.ReviewCreateRequest;
import com.nexra.hrms.nexra.modules.hrms.performance.model.GoalView;
import com.nexra.hrms.nexra.modules.hrms.performance.model.PerformanceSummaryView;
import com.nexra.hrms.nexra.modules.hrms.performance.model.ReviewView;
import com.nexra.hrms.nexra.modules.hrms.performance.security.AuthenticatedPerformanceUser;

import java.util.List;

/**
 * Service contract for tenant-scoped performance goal and review workflows.
 *
 * @author niteshjaitwar
 */
public interface IPerformanceService {

    /**
     * Creates or updates a performance goal for an employee.
     *
     * @param request goal payload
     * @param actor   authenticated user
     * @return saved goal view
     */
    GoalView upsertGoal(GoalUpsertRequest request, AuthenticatedPerformanceUser actor);

    /**
     * Lists goals with optional employee and status filters.
     *
     * @param tenantCode tenant code
     * @param employeeId optional employee filter
     * @param status     optional status filter
     * @param actor      authenticated user
     * @return filtered list of goal views
     */
    List<GoalView> listGoals(String tenantCode, String employeeId, String status, AuthenticatedPerformanceUser actor);

    /**
     * Creates a new performance review cycle for an employee.
     *
     * @param request review creation payload
     * @param actor   authenticated user
     * @return created review view
     */
    ReviewView createReview(ReviewCreateRequest request, AuthenticatedPerformanceUser actor);

    /**
     * Completes a performance review with manager score and comments.
     *
     * @param reviewId review identifier
     * @param request  completion payload
     * @param actor    authenticated user
     * @return updated review view
     */
    ReviewView completeReview(String reviewId, ReviewCompleteRequest request, AuthenticatedPerformanceUser actor);

    /**
     * Lists reviews with optional employee and status filters.
     *
     * @param tenantCode tenant code
     * @param employeeId optional employee filter
     * @param status     optional status filter
     * @param actor      authenticated user
     * @return filtered list of review views
     */
    List<ReviewView> listReviews(String tenantCode, String employeeId, String status, AuthenticatedPerformanceUser actor);

    /**
     * Returns aggregated performance summary for a tenant.
     *
     * @param tenantCode tenant code
     * @param actor      authenticated user
     * @return summary view
     */
    PerformanceSummaryView summary(String tenantCode, AuthenticatedPerformanceUser actor);
}
