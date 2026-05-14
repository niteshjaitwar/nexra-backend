package com.nexra.hrms.nexra.modules.hrms.onboarding.service;

import com.nexra.hrms.nexra.modules.hrms.onboarding.dto.request.OnboardingPlanCreateRequest;
import com.nexra.hrms.nexra.modules.hrms.onboarding.dto.request.OnboardingTaskCompleteRequest;
import com.nexra.hrms.nexra.modules.hrms.onboarding.dto.request.OnboardingTaskCreateRequest;
import com.nexra.hrms.nexra.modules.hrms.onboarding.model.OnboardingPlanView;
import com.nexra.hrms.nexra.modules.hrms.onboarding.model.OnboardingSummaryView;
import com.nexra.hrms.nexra.modules.hrms.onboarding.model.OnboardingTaskView;
import com.nexra.hrms.nexra.modules.hrms.onboarding.security.AuthenticatedOnboardingUser;

import java.util.List;

/**
 * Service contract for tenant-scoped onboarding plan and task workflows.
 *
 * @author niteshjaitwar
 */
public interface IOnboardingService {

    /**
     * Creates a new onboarding plan for an employee.
     *
     * @param request onboarding plan creation payload
     * @param actor   authenticated user
     * @return created plan view
     */
    OnboardingPlanView createPlan(OnboardingPlanCreateRequest request, AuthenticatedOnboardingUser actor);

    /**
     * Adds a task to an existing onboarding plan.
     *
     * @param planId  onboarding plan identifier
     * @param request task creation payload
     * @param actor   authenticated user
     * @return created task view
     */
    OnboardingTaskView addTask(String planId, OnboardingTaskCreateRequest request, AuthenticatedOnboardingUser actor);

    /**
     * Marks a task as completed.
     *
     * @param taskId  task identifier
     * @param request task completion payload
     * @param actor   authenticated user
     * @return updated task view
     */
    OnboardingTaskView completeTask(String taskId, OnboardingTaskCompleteRequest request, AuthenticatedOnboardingUser actor);

    /**
     * Lists onboarding plans with optional employee and status filters.
     *
     * @param tenantCode tenant code
     * @param employeeId optional employee filter
     * @param status     optional status filter
     * @param actor      authenticated user
     * @return filtered list of plan views
     */
    List<OnboardingPlanView> listPlans(String tenantCode, String employeeId, String status, AuthenticatedOnboardingUser actor);

    /**
     * Lists all tasks for a given onboarding plan.
     *
     * @param tenantCode tenant code
     * @param planId     onboarding plan identifier
     * @param actor      authenticated user
     * @return list of task views ordered by creation date
     */
    List<OnboardingTaskView> listTasks(String tenantCode, String planId, AuthenticatedOnboardingUser actor);

    /**
     * Returns aggregated onboarding summary for a tenant.
     *
     * @param tenantCode tenant code
     * @param actor      authenticated user
     * @return summary view
     */
    OnboardingSummaryView summary(String tenantCode, AuthenticatedOnboardingUser actor);
}
