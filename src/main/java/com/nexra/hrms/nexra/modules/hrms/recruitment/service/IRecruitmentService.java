package com.nexra.hrms.nexra.modules.hrms.recruitment.service;

import com.nexra.hrms.nexra.modules.hrms.recruitment.dto.request.CandidateCreateRequest;
import com.nexra.hrms.nexra.modules.hrms.recruitment.dto.request.CandidateStageChangeRequest;
import com.nexra.hrms.nexra.modules.hrms.recruitment.dto.request.JobUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.recruitment.model.CandidateStageHistoryView;
import com.nexra.hrms.nexra.modules.hrms.recruitment.model.CandidateView;
import com.nexra.hrms.nexra.modules.hrms.recruitment.model.JobView;
import com.nexra.hrms.nexra.modules.hrms.recruitment.model.RecruitmentSummaryView;
import com.nexra.hrms.nexra.modules.hrms.recruitment.security.AuthenticatedRecruitmentUser;

import java.util.List;
import org.springframework.data.domain.Pageable;
import com.nexra.hrms.nexra.common.api.PageResponse;

/**
 * Service contract for tenant-scoped job and candidate recruitment workflows.
 *
 * @author niteshjaitwar
 */
public interface IRecruitmentService {

    /**
     * Creates or updates a job posting.
     *
     * @param request job payload
     * @param actor   authenticated user
     * @return saved job view
     */
    JobView upsertJob(JobUpsertRequest request, AuthenticatedRecruitmentUser actor);

    /**
     * Lists job postings with optional status filter.
     *
     * @param tenantCode tenant code
     * @param status     optional status filter
     * @param actor      authenticated user
     * @return filtered list of job views
     */
    List<JobView> listJobs(String tenantCode, String status, AuthenticatedRecruitmentUser actor);

    /** Paginated job listing. */
    PageResponse<JobView> listJobs(String tenantCode, String status, AuthenticatedRecruitmentUser actor, Pageable pageable);

    /**
     * Registers a new candidate for a job.
     *
     * @param request candidate creation payload
     * @param actor   authenticated user
     * @return created candidate view
     */
    CandidateView createCandidate(CandidateCreateRequest request, AuthenticatedRecruitmentUser actor);

    /**
     * Transitions a candidate to a new pipeline stage.
     *
     * @param candidateId candidate identifier
     * @param request     stage change payload
     * @param actor       authenticated user
     * @return updated candidate view
     */
    CandidateView changeStage(String candidateId, CandidateStageChangeRequest request, AuthenticatedRecruitmentUser actor);

    /**
     * Lists candidates with optional job and stage filters.
     *
     * @param tenantCode tenant code
     * @param jobId      optional job filter
     * @param stage      optional stage filter
     * @param actor      authenticated user
     * @return filtered list of candidate views
     */
    List<CandidateView> listCandidates(String tenantCode, String jobId, String stage, AuthenticatedRecruitmentUser actor);

    /** Paginated candidate listing. */
    PageResponse<CandidateView> listCandidates(String tenantCode, String jobId, String stage, AuthenticatedRecruitmentUser actor, Pageable pageable);

    /**
     * Returns the stage change history for a candidate.
     *
     * @param tenantCode  tenant code
     * @param candidateId candidate identifier
     * @param actor       authenticated user
     * @return history ordered by creation date descending
     */
    List<CandidateStageHistoryView> history(String tenantCode, String candidateId, AuthenticatedRecruitmentUser actor);

    /**
     * Returns aggregated recruitment summary for a tenant.
     *
     * @param tenantCode tenant code
     * @param actor      authenticated user
     * @return summary view
     */
    RecruitmentSummaryView summary(String tenantCode, AuthenticatedRecruitmentUser actor);
}
