package com.nexra.hrms.nexra.modules.hrms.performance.service.impl;

import com.nexra.hrms.nexra.common.audit.AuditEventRecord;
import com.nexra.hrms.nexra.common.audit.AuditEventService;
import com.nexra.hrms.nexra.modules.hrms.performance.dto.request.GoalUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.performance.dto.request.ReviewCompleteRequest;
import com.nexra.hrms.nexra.modules.hrms.performance.dto.request.ReviewCreateRequest;
import com.nexra.hrms.nexra.modules.hrms.performance.entity.GoalEntity;
import com.nexra.hrms.nexra.modules.hrms.performance.entity.ReviewEntity;
import com.nexra.hrms.nexra.modules.hrms.performance.exception.PerformanceBusinessException;
import com.nexra.hrms.nexra.modules.hrms.performance.exception.PerformanceResourceNotFoundException;
import com.nexra.hrms.nexra.modules.hrms.performance.model.GoalView;
import com.nexra.hrms.nexra.modules.hrms.performance.model.PerformanceSummaryView;
import com.nexra.hrms.nexra.modules.hrms.performance.model.ReviewView;
import com.nexra.hrms.nexra.modules.hrms.performance.repository.GoalRepository;
import com.nexra.hrms.nexra.modules.hrms.performance.repository.ReviewRepository;
import com.nexra.hrms.nexra.modules.hrms.performance.security.AuthenticatedPerformanceUser;
import com.nexra.hrms.nexra.modules.hrms.performance.service.IPerformanceService;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements tenant-scoped goal and review performance workflows.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PerformanceServiceImpl implements IPerformanceService {

    private final GoalRepository goalRepository;
    private final ReviewRepository reviewRepository;
    private final AuditEventService auditEventService;

    @Override
    @Transactional
    public GoalView upsertGoal(
        final GoalUpsertRequest request,
        final AuthenticatedPerformanceUser actor
    ) {
        assertTenant(request.tenantCode(), actor);

        GoalEntity entity = request.goalId() == null || request.goalId().isBlank()
            ? new GoalEntity()
            : goalRepository.findByTenantCodeAndGoalId(normalizeTenant(request.tenantCode()), request.goalId())
                .orElseGet(GoalEntity::new);

        boolean isNewGoal = entity.getGoalId() == null;
        if (isNewGoal) {
            entity.setGoalId(UUID.randomUUID().toString());
            entity.setTenantCode(normalizeTenant(request.tenantCode()));
            entity.setCreatedBy(actorName(actor));
        }

        entity.setEmployeeId(trim(request.employeeId()));
        entity.setTitle(trim(request.title()));
        entity.setDescription(blankToNull(request.description()));
        entity.setTargetDate(request.targetDate());
        entity.setStatus(trim(request.status()).toUpperCase());
        entity.setUpdatedBy(actorName(actor));
        log.info("PerformanceServiceImpl - upsertGoal - tenantCode={}, employeeId={}, isNew={}", request.tenantCode(), request.employeeId(), isNewGoal);
        GoalEntity saved = goalRepository.save(entity);
        recordAudit(saved.getTenantCode(), isNewGoal ? "CREATE_GOAL" : "UPDATE_GOAL", actor, "GOAL", saved.getGoalId());
        return toGoalView(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalView> listGoals(
        final String tenantCode,
        final String employeeId,
        final String status,
        final AuthenticatedPerformanceUser actor
    ) {
        assertTenant(tenantCode, actor);
        String tenant = normalizeTenant(tenantCode);
        String employeeFilter = blankToNull(employeeId);
        String statusFilter = blankToNullUpper(status);
        return goalRepository.findByTenantCodeOrderByCreatedAtDesc(tenant).stream()
            .filter(goal -> employeeFilter == null || employeeFilter.equals(goal.getEmployeeId()))
            .filter(goal -> statusFilter == null || statusFilter.equalsIgnoreCase(goal.getStatus()))
            .map(this::toGoalView)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public com.nexra.hrms.nexra.common.api.PageResponse<GoalView> listGoals(
        final String tenantCode,
        final String employeeId,
        final String status,
        final AuthenticatedPerformanceUser actor,
        final org.springframework.data.domain.Pageable pageable
    ) {
        assertTenant(tenantCode, actor);
        String tenant = normalizeTenant(tenantCode);
        String employeeFilter = blankToNull(employeeId);
        String statusFilter = blankToNullUpper(status);
        org.springframework.data.domain.Page<GoalEntity> page;
        if (employeeFilter == null && statusFilter == null) {
            page = goalRepository.findByTenantCode(tenant, pageable);
        } else if (employeeFilter != null && statusFilter == null) {
            page = goalRepository.findByTenantCodeAndEmployeeId(tenant, employeeFilter, pageable);
        } else if (employeeFilter == null) {
            page = goalRepository.findByTenantCodeAndStatusIgnoreCase(tenant, statusFilter, pageable);
        } else {
            page = goalRepository.findByTenantCodeAndEmployeeIdAndStatusIgnoreCase(tenant, employeeFilter, statusFilter, pageable);
        }
        return com.nexra.hrms.nexra.common.api.PageResponse.map(
            com.nexra.hrms.nexra.common.api.PageResponse.from(page), this::toGoalView
        );
    }

    @Override
    @Transactional
    public ReviewView createReview(
        final ReviewCreateRequest request,
        final AuthenticatedPerformanceUser actor
    ) {
        assertTenant(request.tenantCode(), actor);
        log.info("PerformanceServiceImpl - createReview - tenantCode={}, employeeId={}", request.tenantCode(), request.employeeId());

        ReviewEntity entity = new ReviewEntity();
        entity.setReviewId(UUID.randomUUID().toString());
        entity.setTenantCode(normalizeTenant(request.tenantCode()));
        entity.setEmployeeId(trim(request.employeeId()));
        entity.setReviewCycle(trim(request.reviewCycle()));
        entity.setStatus("IN_PROGRESS");
        entity.setEmployeeComments(blankToNull(request.employeeComments()));
        entity.setCreatedBy(actorName(actor));
        entity.setUpdatedBy(actorName(actor));
        ReviewEntity saved = reviewRepository.save(entity);
        recordAudit(saved.getTenantCode(), "CREATE_REVIEW", actor, "PERFORMANCE_REVIEW", saved.getReviewId());
        return toReviewView(saved);
    }

    @Override
    @Transactional
    public ReviewView completeReview(
        final String reviewId,
        final ReviewCompleteRequest request,
        final AuthenticatedPerformanceUser actor
    ) {
        assertTenant(request.tenantCode(), actor);
        ReviewEntity entity = reviewRepository.findByTenantCodeAndReviewId(normalizeTenant(request.tenantCode()), reviewId)
            .orElseThrow(() -> new PerformanceResourceNotFoundException("Performance review not found: " + reviewId));

        if ("COMPLETED".equalsIgnoreCase(entity.getStatus())) {
            throw new PerformanceBusinessException("Performance review already completed: " + reviewId);
        }

        entity.setManagerScore(request.managerScore());
        entity.setManagerComments(blankToNull(request.managerComments()));
        entity.setStatus("COMPLETED");
        entity.setUpdatedBy(actorName(actor));
        log.info("PerformanceServiceImpl - completeReview - tenantCode={}, reviewId={}", request.tenantCode(), reviewId);
        ReviewEntity saved = reviewRepository.save(entity);
        recordAudit(saved.getTenantCode(), "COMPLETE_REVIEW", actor, "PERFORMANCE_REVIEW", saved.getReviewId());
        return toReviewView(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewView> listReviews(
        final String tenantCode,
        final String employeeId,
        final String status,
        final AuthenticatedPerformanceUser actor
    ) {
        assertTenant(tenantCode, actor);
        String tenant = normalizeTenant(tenantCode);
        String employeeFilter = blankToNull(employeeId);
        String statusFilter = blankToNullUpper(status);
        return reviewRepository.findByTenantCodeOrderByCreatedAtDesc(tenant).stream()
            .filter(review -> employeeFilter == null || employeeFilter.equals(review.getEmployeeId()))
            .filter(review -> statusFilter == null || statusFilter.equalsIgnoreCase(review.getStatus()))
            .map(this::toReviewView)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public com.nexra.hrms.nexra.common.api.PageResponse<ReviewView> listReviews(
        final String tenantCode,
        final String employeeId,
        final String status,
        final AuthenticatedPerformanceUser actor,
        final org.springframework.data.domain.Pageable pageable
    ) {
        assertTenant(tenantCode, actor);
        String tenant = normalizeTenant(tenantCode);
        String employeeFilter = blankToNull(employeeId);
        String statusFilter = blankToNullUpper(status);
        org.springframework.data.domain.Page<ReviewEntity> page;
        if (employeeFilter == null && statusFilter == null) {
            page = reviewRepository.findByTenantCode(tenant, pageable);
        } else if (employeeFilter != null && statusFilter == null) {
            page = reviewRepository.findByTenantCodeAndEmployeeId(tenant, employeeFilter, pageable);
        } else if (employeeFilter == null) {
            page = reviewRepository.findByTenantCodeAndStatusIgnoreCase(tenant, statusFilter, pageable);
        } else {
            page = reviewRepository.findByTenantCodeAndEmployeeIdAndStatusIgnoreCase(tenant, employeeFilter, statusFilter, pageable);
        }
        return com.nexra.hrms.nexra.common.api.PageResponse.map(
            com.nexra.hrms.nexra.common.api.PageResponse.from(page), this::toReviewView
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PerformanceSummaryView summary(final String tenantCode, final AuthenticatedPerformanceUser actor) {
        assertTenant(tenantCode, actor);
        String tenant = normalizeTenant(tenantCode);
        return new PerformanceSummaryView(
            tenant,
            goalRepository.countByTenantCode(tenant),
            goalRepository.countByTenantCodeAndStatus(tenant, "ACTIVE"),
            reviewRepository.countByTenantCode(tenant),
            reviewRepository.countByTenantCodeAndStatus(tenant, "COMPLETED")
        );
    }

    // ---- private helpers ----

    private GoalView toGoalView(final GoalEntity entity) {
        return new GoalView(
            entity.getGoalId(),
            entity.getTenantCode(),
            entity.getEmployeeId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getTargetDate(),
            entity.getStatus()
        );
    }

    private ReviewView toReviewView(final ReviewEntity entity) {
        return new ReviewView(
            entity.getReviewId(),
            entity.getTenantCode(),
            entity.getEmployeeId(),
            entity.getReviewCycle(),
            entity.getStatus(),
            entity.getManagerScore(),
            entity.getEmployeeComments(),
            entity.getManagerComments()
        );
    }

    private void assertTenant(final String tenantCode, final AuthenticatedPerformanceUser actor) {
        if (tenantCode == null || tenantCode.isBlank()) {
            throw new PerformanceBusinessException("tenantCode is required");
        }
        if (!actor.tenantCode().equalsIgnoreCase(tenantCode)) {
            throw new PerformanceBusinessException("Token tenant does not match requested tenant");
        }
    }

    private String actorName(final AuthenticatedPerformanceUser actor) {
        return actor.email() != null ? actor.email() : String.valueOf(actor.userId());
    }

    private void recordAudit(
        final String tenantCode,
        final String action,
        final AuthenticatedPerformanceUser actor,
        final String targetType,
        final String targetId
    ) {
        auditEventService.record(AuditEventRecord.of(tenantCode, "PERFORMANCE", action, "SUCCESS")
            .withActor(actor.email(), actor.userId().toString())
            .withTarget(targetType, targetId));
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
