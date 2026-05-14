package com.nexra.hrms.nexra.modules.hrms.onboarding.service.impl;

import com.nexra.hrms.nexra.modules.hrms.onboarding.dto.request.OnboardingPlanCreateRequest;
import com.nexra.hrms.nexra.modules.hrms.onboarding.dto.request.OnboardingTaskCompleteRequest;
import com.nexra.hrms.nexra.modules.hrms.onboarding.dto.request.OnboardingTaskCreateRequest;
import com.nexra.hrms.nexra.modules.hrms.onboarding.entity.OnboardingPlanEntity;
import com.nexra.hrms.nexra.modules.hrms.onboarding.entity.OnboardingTaskEntity;
import com.nexra.hrms.nexra.modules.hrms.onboarding.exception.OnboardingBusinessException;
import com.nexra.hrms.nexra.modules.hrms.onboarding.exception.OnboardingResourceNotFoundException;
import com.nexra.hrms.nexra.modules.hrms.onboarding.model.OnboardingPlanView;
import com.nexra.hrms.nexra.modules.hrms.onboarding.model.OnboardingSummaryView;
import com.nexra.hrms.nexra.modules.hrms.onboarding.model.OnboardingTaskView;
import com.nexra.hrms.nexra.modules.hrms.onboarding.repository.OnboardingPlanRepository;
import com.nexra.hrms.nexra.modules.hrms.onboarding.repository.OnboardingTaskRepository;
import com.nexra.hrms.nexra.modules.hrms.onboarding.security.AuthenticatedOnboardingUser;
import com.nexra.hrms.nexra.modules.hrms.onboarding.service.IOnboardingService;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements tenant-scoped onboarding plan and task workflows.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingServiceImpl implements IOnboardingService {

    private final OnboardingPlanRepository planRepository;
    private final OnboardingTaskRepository taskRepository;

    @Override
    @Transactional
    public OnboardingPlanView createPlan(
        final OnboardingPlanCreateRequest request,
        final AuthenticatedOnboardingUser actor
    ) {
        assertTenant(request.tenantCode(), actor);
        log.info("OnboardingServiceImpl - createPlan - tenantCode={}, employeeId={}", request.tenantCode(), request.employeeId());

        OnboardingPlanEntity entity = new OnboardingPlanEntity();
        entity.setPlanId(UUID.randomUUID().toString());
        entity.setTenantCode(normalizeTenant(request.tenantCode()));
        entity.setEmployeeId(trim(request.employeeId()));
        entity.setPlanName(trim(request.planName()));
        entity.setStatus("ACTIVE");
        entity.setCreatedBy(actorName(actor));
        entity.setUpdatedBy(actorName(actor));
        return toPlanView(planRepository.save(entity));
    }

    @Override
    @Transactional
    public OnboardingTaskView addTask(
        final String planId,
        final OnboardingTaskCreateRequest request,
        final AuthenticatedOnboardingUser actor
    ) {
        assertTenant(request.tenantCode(), actor);
        planRepository.findByTenantCodeAndPlanId(normalizeTenant(request.tenantCode()), planId)
            .orElseThrow(() -> new OnboardingResourceNotFoundException("Onboarding plan not found: " + planId));
        log.info("OnboardingServiceImpl - addTask - tenantCode={}, planId={}", request.tenantCode(), planId);

        OnboardingTaskEntity entity = new OnboardingTaskEntity();
        entity.setTaskId(UUID.randomUUID().toString());
        entity.setTenantCode(normalizeTenant(request.tenantCode()));
        entity.setPlanId(planId);
        entity.setTaskName(trim(request.taskName()));
        entity.setOwnerTeam(blankToNull(request.ownerTeam()));
        entity.setStatus("PENDING");
        entity.setCreatedBy(actorName(actor));
        entity.setUpdatedBy(actorName(actor));
        return toTaskView(taskRepository.save(entity));
    }

    @Override
    @Transactional
    public OnboardingTaskView completeTask(
        final String taskId,
        final OnboardingTaskCompleteRequest request,
        final AuthenticatedOnboardingUser actor
    ) {
        assertTenant(request.tenantCode(), actor);
        OnboardingTaskEntity entity = taskRepository.findByTenantCodeAndTaskId(normalizeTenant(request.tenantCode()), taskId)
            .orElseThrow(() -> new OnboardingResourceNotFoundException("Onboarding task not found: " + taskId));

        if ("COMPLETED".equalsIgnoreCase(entity.getStatus())) {
            throw new OnboardingBusinessException("Onboarding task already completed: " + taskId);
        }

        entity.setStatus("COMPLETED");
        entity.setUpdatedBy(actorName(actor));
        log.info("OnboardingServiceImpl - completeTask - tenantCode={}, taskId={}", request.tenantCode(), taskId);
        return toTaskView(taskRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OnboardingPlanView> listPlans(
        final String tenantCode,
        final String employeeId,
        final String status,
        final AuthenticatedOnboardingUser actor
    ) {
        assertTenant(tenantCode, actor);
        String employeeFilter = blankToNull(employeeId);
        String statusFilter = blankToNullUpper(status);
        return planRepository.findByTenantCodeOrderByCreatedAtDesc(normalizeTenant(tenantCode)).stream()
            .filter(plan -> employeeFilter == null || employeeFilter.equals(plan.getEmployeeId()))
            .filter(plan -> statusFilter == null || statusFilter.equalsIgnoreCase(plan.getStatus()))
            .map(this::toPlanView)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OnboardingTaskView> listTasks(
        final String tenantCode,
        final String planId,
        final AuthenticatedOnboardingUser actor
    ) {
        assertTenant(tenantCode, actor);
        planRepository.findByTenantCodeAndPlanId(normalizeTenant(tenantCode), planId)
            .orElseThrow(() -> new OnboardingResourceNotFoundException("Onboarding plan not found: " + planId));
        return taskRepository.findByTenantCodeAndPlanIdOrderByCreatedAtAsc(normalizeTenant(tenantCode), planId).stream()
            .map(this::toTaskView)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OnboardingSummaryView summary(final String tenantCode, final AuthenticatedOnboardingUser actor) {
        assertTenant(tenantCode, actor);
        String tenant = normalizeTenant(tenantCode);
        return new OnboardingSummaryView(
            tenant,
            planRepository.countByTenantCode(tenant),
            planRepository.countByTenantCodeAndStatus(tenant, "ACTIVE"),
            taskRepository.countByTenantCode(tenant),
            taskRepository.countByTenantCodeAndStatus(tenant, "COMPLETED")
        );
    }

    // ---- private helpers ----

    private OnboardingPlanView toPlanView(final OnboardingPlanEntity entity) {
        return new OnboardingPlanView(
            entity.getPlanId(),
            entity.getTenantCode(),
            entity.getEmployeeId(),
            entity.getPlanName(),
            entity.getStatus()
        );
    }

    private OnboardingTaskView toTaskView(final OnboardingTaskEntity entity) {
        return new OnboardingTaskView(
            entity.getTaskId(),
            entity.getTenantCode(),
            entity.getPlanId(),
            entity.getTaskName(),
            entity.getOwnerTeam(),
            entity.getStatus()
        );
    }

    private void assertTenant(final String tenantCode, final AuthenticatedOnboardingUser actor) {
        if (tenantCode == null || tenantCode.isBlank()) {
            throw new OnboardingBusinessException("tenantCode is required");
        }
        if (!actor.tenantCode().equalsIgnoreCase(tenantCode)) {
            throw new OnboardingBusinessException("Token tenant does not match requested tenant");
        }
    }

    private String actorName(final AuthenticatedOnboardingUser actor) {
        return actor.email() != null ? actor.email() : String.valueOf(actor.userId());
    }

    private String normalizeTenant(final String value) {
        return trim(value).toUpperCase();
    }

    private String blankToNull(final String value) {
        String trimmed = trim(value);
        return trimmed == null || trimmed.isBlank() ? null : trimmed;
    }

    private String blankToNullUpper(final String value) {
        String trimmed = blankToNull(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }

    private String trim(final String value) {
        return value == null ? null : value.trim();
    }
}
