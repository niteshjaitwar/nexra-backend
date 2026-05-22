package com.nexra.hrms.nexra.modules.hrms.recruitment.service.impl;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.modules.hrms.recruitment.dto.request.CandidateCreateRequest;
import com.nexra.hrms.nexra.modules.hrms.recruitment.dto.request.CandidateStageChangeRequest;
import com.nexra.hrms.nexra.modules.hrms.recruitment.dto.request.JobUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.recruitment.entity.CandidateEntity;
import com.nexra.hrms.nexra.modules.hrms.recruitment.entity.CandidateStageHistoryEntity;
import com.nexra.hrms.nexra.modules.hrms.recruitment.entity.JobEntity;
import com.nexra.hrms.nexra.modules.hrms.recruitment.exception.RecruitmentBusinessException;
import com.nexra.hrms.nexra.modules.hrms.recruitment.exception.RecruitmentResourceNotFoundException;
import com.nexra.hrms.nexra.modules.hrms.recruitment.model.CandidateStageHistoryView;
import com.nexra.hrms.nexra.modules.hrms.recruitment.model.CandidateView;
import com.nexra.hrms.nexra.modules.hrms.recruitment.model.JobView;
import com.nexra.hrms.nexra.modules.hrms.recruitment.model.RecruitmentSummaryView;
import com.nexra.hrms.nexra.modules.hrms.recruitment.repository.CandidateRepository;
import com.nexra.hrms.nexra.modules.hrms.recruitment.repository.CandidateStageHistoryRepository;
import com.nexra.hrms.nexra.modules.hrms.recruitment.repository.JobRepository;
import com.nexra.hrms.nexra.modules.hrms.recruitment.security.AuthenticatedRecruitmentUser;
import com.nexra.hrms.nexra.modules.hrms.recruitment.service.IRecruitmentService;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements tenant-scoped job and candidate recruitment workflows.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecruitmentServiceImpl implements IRecruitmentService {

    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateStageHistoryRepository historyRepository;

    @Override
    @Transactional
    public JobView upsertJob(final JobUpsertRequest request, final AuthenticatedRecruitmentUser actor) {
        assertTenant(request.tenantCode(), actor);

        JobEntity entity = request.jobId() == null || request.jobId().isBlank()
            ? new JobEntity()
            : jobRepository.findByTenantCodeAndJobId(normalizeTenant(request.tenantCode()), request.jobId())
                .orElseGet(JobEntity::new);

        boolean isNewJob = entity.getJobId() == null;
        if (isNewJob) {
            entity.setJobId(UUID.randomUUID().toString());
            entity.setTenantCode(normalizeTenant(request.tenantCode()));
            entity.setCreatedBy(actorName(actor));
        }

        entity.setTitle(trim(request.title()));
        entity.setDepartment(blankToNull(request.department()));
        entity.setLocation(blankToNull(request.location()));
        entity.setStatus(trim(request.status()).toUpperCase());
        entity.setUpdatedBy(actorName(actor));
        log.info("RecruitmentServiceImpl - upsertJob - tenantCode={}, isNew={}", request.tenantCode(), isNewJob);
        return toJobView(jobRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobView> listJobs(
        final String tenantCode,
        final String status,
        final AuthenticatedRecruitmentUser actor
    ) {
        assertTenant(tenantCode, actor);
        String tenant = normalizeTenant(tenantCode);
        String statusFilter = blankToNullUpper(status);
        return jobRepository.findByTenantCodeOrderByCreatedAtDesc(tenant).stream()
            .filter(job -> statusFilter == null || statusFilter.equalsIgnoreCase(job.getStatus()))
            .map(this::toJobView)
            .toList();
    }

    @Override
    @Transactional
    public CandidateView createCandidate(
        final CandidateCreateRequest request,
        final AuthenticatedRecruitmentUser actor
    ) {
        assertTenant(request.tenantCode(), actor);
        String tenant = normalizeTenant(request.tenantCode());
        jobRepository.findByTenantCodeAndJobId(tenant, request.jobId())
            .orElseThrow(() -> new RecruitmentResourceNotFoundException("Recruitment job not found: " + request.jobId()));
        assertCandidateIdentityUnique(tenant, request.email(), request.phone());

        CandidateEntity entity = new CandidateEntity();
        entity.setCandidateId(UUID.randomUUID().toString());
        entity.setTenantCode(tenant);
        entity.setJobId(trim(request.jobId()));
        entity.setFullName(trim(request.fullName()));
        entity.setEmail(blankToNull(request.email()));
        entity.setPhone(blankToNull(request.phone()));
        entity.setStage("APPLIED");
        entity.setCreatedBy(actorName(actor));
        entity.setUpdatedBy(actorName(actor));

        CandidateEntity saved = candidateRepository.save(entity);
        addHistory(tenant, saved.getCandidateId(), null, "APPLIED", "Candidate created", actor);
        log.info("RecruitmentServiceImpl - createCandidate - tenantCode={}, jobId={}", tenant, request.jobId());
        return toCandidateView(saved);
    }

    @Override
    @Transactional
    public CandidateView changeStage(
        final String candidateId,
        final CandidateStageChangeRequest request,
        final AuthenticatedRecruitmentUser actor
    ) {
        assertTenant(request.tenantCode(), actor);
        String tenant = normalizeTenant(request.tenantCode());
        CandidateEntity entity = findCandidate(tenant, candidateId);

        String fromStage = entity.getStage();
        String toStage = trim(request.stage()).toUpperCase();
        if (toStage.equalsIgnoreCase(fromStage)) {
            throw new RecruitmentBusinessException("Candidate is already in stage: " + toStage);
        }

        entity.setStage(toStage);
        entity.setUpdatedBy(actorName(actor));
        CandidateEntity saved = candidateRepository.save(entity);
        addHistory(tenant, candidateId, fromStage, toStage, blankToNull(request.comment()), actor);
        log.info("RecruitmentServiceImpl - changeStage - tenantCode={}, candidateId={}, from={}, to={}", tenant, candidateId, fromStage, toStage);
        return toCandidateView(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidateView> listCandidates(
        final String tenantCode,
        final String jobId,
        final String stage,
        final AuthenticatedRecruitmentUser actor
    ) {
        assertTenant(tenantCode, actor);
        String tenant = normalizeTenant(tenantCode);
        String jobFilter = blankToNull(jobId);
        String stageFilter = blankToNullUpper(stage);
        return candidateRepository.findByTenantCodeOrderByCreatedAtDesc(tenant).stream()
            .filter(candidate -> jobFilter == null || jobFilter.equals(candidate.getJobId()))
            .filter(candidate -> stageFilter == null || stageFilter.equalsIgnoreCase(candidate.getStage()))
            .map(this::toCandidateView)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidateStageHistoryView> history(
        final String tenantCode,
        final String candidateId,
        final AuthenticatedRecruitmentUser actor
    ) {
        assertTenant(tenantCode, actor);
        String tenant = normalizeTenant(tenantCode);
        findCandidate(tenant, candidateId);
        return historyRepository.findByTenantCodeAndCandidateIdOrderByCreatedAtDesc(tenant, candidateId).stream()
            .map(this::toHistoryView)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RecruitmentSummaryView summary(final String tenantCode, final AuthenticatedRecruitmentUser actor) {
        assertTenant(tenantCode, actor);
        String tenant = normalizeTenant(tenantCode);
        return new RecruitmentSummaryView(
            tenant,
            jobRepository.countByTenantCode(tenant),
            jobRepository.countByTenantCodeAndStatus(tenant, "OPEN"),
            candidateRepository.countByTenantCode(tenant),
            candidateRepository.countByTenantCodeAndStage(tenant, "HIRED"),
            historyRepository.countByTenantCode(tenant)
        );
    }

    // ---- private helpers ----

    private void addHistory(
        final String tenantCode,
        final String candidateId,
        final String fromStage,
        final String toStage,
        final String comment,
        final AuthenticatedRecruitmentUser actor
    ) {
        CandidateStageHistoryEntity entity = new CandidateStageHistoryEntity();
        entity.setHistoryId(UUID.randomUUID().toString());
        entity.setTenantCode(tenantCode);
        entity.setCandidateId(candidateId);
        entity.setFromStage(fromStage);
        entity.setToStage(toStage);
        entity.setComment(comment);
        entity.setCreatedBy(actorName(actor));
        entity.setUpdatedBy(actorName(actor));
        historyRepository.save(entity);
    }

    private CandidateEntity findCandidate(final String tenantCode, final String candidateId) {
        return candidateRepository.findByTenantCodeAndCandidateId(tenantCode, candidateId)
            .orElseThrow(() -> new RecruitmentResourceNotFoundException("Recruitment candidate not found: " + candidateId));
    }

    private void assertCandidateIdentityUnique(
        final String tenantCode,
        final String email,
        final String phone
    ) {
        final String normalizedEmail = blankToNull(email);
        if (normalizedEmail != null && candidateRepository.existsByTenantCodeAndEmailIgnoreCase(tenantCode, normalizedEmail)) {
            throw new RecruitmentBusinessException("Candidate already exists for tenant with the same email.");
        }
        final String normalizedPhone = blankToNull(phone);
        if (normalizedPhone != null && candidateRepository.existsByTenantCodeAndPhone(tenantCode, normalizedPhone)) {
            throw new RecruitmentBusinessException("Candidate already exists for tenant with the same phone.");
        }
    }

    private JobView toJobView(final JobEntity entity) {
        return new JobView(
            entity.getJobId(),
            entity.getTenantCode(),
            entity.getTitle(),
            entity.getDepartment(),
            entity.getLocation(),
            entity.getStatus()
        );
    }

    private CandidateView toCandidateView(final CandidateEntity entity) {
        return new CandidateView(
            entity.getCandidateId(),
            entity.getTenantCode(),
            entity.getJobId(),
            entity.getFullName(),
            entity.getEmail(),
            entity.getPhone(),
            entity.getStage()
        );
    }

    private CandidateStageHistoryView toHistoryView(final CandidateStageHistoryEntity entity) {
        return new CandidateStageHistoryView(
            entity.getHistoryId(),
            entity.getTenantCode(),
            entity.getCandidateId(),
            entity.getFromStage(),
            entity.getToStage(),
            entity.getComment()
        );
    }

    private void assertTenant(final String tenantCode, final AuthenticatedRecruitmentUser actor) {
        if (tenantCode == null || tenantCode.isBlank()) {
            throw new RecruitmentBusinessException("tenantCode is required");
        }
        if (!actor.tenantCode().equalsIgnoreCase(tenantCode)) {
            throw new RecruitmentBusinessException("Token tenant does not match requested tenant");
        }
    }

    private String actorName(final AuthenticatedRecruitmentUser actor) {
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

    @Override
    public PageResponse<JobView> listJobs(
        final String tenantCode,
        final String status,
        final AuthenticatedRecruitmentUser actor,
        final Pageable pageable
    ) {
        assertTenant(tenantCode, actor);
        String tenant = normalizeTenant(tenantCode);
        String statusFilter = blankToNullUpper(status);
        Page<JobEntity> jobsPage = statusFilter == null
            ? jobRepository.findByTenantCode(tenant, pageable)
            : jobRepository.findByTenantCodeAndStatusIgnoreCase(tenant, statusFilter, pageable);
        return PageResponse.from(jobsPage.map(this::toJobView));
    }

    @Override
    public PageResponse<CandidateView> listCandidates(
        final String tenantCode,
        final String jobId,
        final String stage,
        final AuthenticatedRecruitmentUser actor,
        final Pageable pageable
    ) {
        assertTenant(tenantCode, actor);
        String tenant = normalizeTenant(tenantCode);
        String jobFilter = blankToNull(jobId);
        String stageFilter = blankToNullUpper(stage);
        Page<CandidateEntity> candidatesPage;
        if (jobFilter != null && stageFilter != null) {
            candidatesPage = candidateRepository.findByTenantCodeAndJobIdAndStageIgnoreCase(
                tenant, jobFilter, stageFilter, pageable
            );
        } else if (jobFilter != null) {
            candidatesPage = candidateRepository.findByTenantCodeAndJobId(tenant, jobFilter, pageable);
        } else if (stageFilter != null) {
            candidatesPage = candidateRepository.findByTenantCodeAndStageIgnoreCase(tenant, stageFilter, pageable);
        } else {
            candidatesPage = candidateRepository.findByTenantCode(tenant, pageable);
        }
        return PageResponse.from(candidatesPage.map(this::toCandidateView));
    }
}
