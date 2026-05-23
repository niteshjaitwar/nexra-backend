package com.nexra.hrms.nexra.modules.hrms.recruitment.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.modules.hrms.recruitment.dto.request.CandidateCreateRequest;
import com.nexra.hrms.nexra.modules.hrms.recruitment.dto.request.CandidateStageChangeRequest;
import com.nexra.hrms.nexra.modules.hrms.recruitment.dto.request.JobUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.recruitment.exception.RecruitmentForbiddenException;
import com.nexra.hrms.nexra.modules.hrms.recruitment.exception.RecruitmentUnauthorizedException;
import com.nexra.hrms.nexra.modules.hrms.recruitment.model.CandidateStageHistoryView;
import com.nexra.hrms.nexra.modules.hrms.recruitment.model.CandidateView;
import com.nexra.hrms.nexra.modules.hrms.recruitment.model.JobView;
import com.nexra.hrms.nexra.modules.hrms.recruitment.model.RecruitmentSummaryView;
import com.nexra.hrms.nexra.modules.hrms.recruitment.security.AuthenticatedRecruitmentUser;
import com.nexra.hrms.nexra.modules.hrms.recruitment.security.RecruitmentAuthFilter;
import com.nexra.hrms.nexra.modules.hrms.recruitment.service.IRecruitmentService;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Exposes tenant-scoped recruitment APIs for jobs, candidates, and hiring summary.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Tag(name = "Recruitment", description = "Recruitment APIs.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recruitment")
@Validated
public class RecruitmentController {

    private final IRecruitmentService recruitmentService;

    @Operation(summary = "GET /api/v1/recruitment/status", description = "Processes GET requests for /api/v1/recruitment/status.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        return ResponseEntity.ok(ApiResponse.success("recruitment service is available.", Map.of(
            "service", "recruitment",
            "timestamp", Instant.now().toString(),
            "state", "UP"
        )));
    }

    @Operation(summary = "GET /api/v1/recruitment/capabilities", description = "Processes GET requests for /api/v1/recruitment/capabilities.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/capabilities")
    public ResponseEntity<ApiResponse<Map<String, Object>>> capabilities() {
        return ResponseEntity.ok(ApiResponse.success("recruitment capabilities fetched successfully.", Map.of(
            "domains", List.of("jobs", "candidates", "candidate-stage-history"),
            "auth", "JWT tenant-scoped",
            "storage", "MySQL + Flyway"
        )));
    }

    @Operation(summary = "PUT /api/v1/recruitment/jobs", description = "Processes PUT requests for /api/v1/recruitment/jobs.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PutMapping("/jobs")
    public ResponseEntity<ApiResponse<JobView>> upsertJob(
        @Valid @RequestBody final JobUpsertRequest request,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedRecruitmentUser actor = currentUser(httpRequest);
        requireAdmin(actor);
        return ResponseEntity.ok(ApiResponse.success(
            "Job upserted successfully.",
            recruitmentService.upsertJob(request, actor)
        ));
    }

    @Operation(summary = "GET /api/v1/recruitment/jobs", description = "Processes GET requests for /api/v1/recruitment/jobs.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<com.nexra.hrms.nexra.common.api.PageResponse<JobView>>> jobs(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        @RequestParam(required = false) final String status,
        @RequestParam(defaultValue = "0") @Min(0) final int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) final int size,
        final HttpServletRequest httpRequest
    ) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
            page, Math.min(size, 100), org.springframework.data.domain.Sort.by("createdAt").descending()
        );
        return ResponseEntity.ok(ApiResponse.success(
            "Jobs fetched successfully.",
            recruitmentService.listJobs(tenantCode, status, currentUser(httpRequest), pageable)
        ));
    }

    @Operation(summary = "POST /api/v1/recruitment/candidates", description = "Processes POST requests for /api/v1/recruitment/candidates.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping("/candidates")
    public ResponseEntity<ApiResponse<CandidateView>> createCandidate(
        @Valid @RequestBody final CandidateCreateRequest request,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedRecruitmentUser actor = currentUser(httpRequest);
        requireAdmin(actor);
        return ResponseEntity.ok(ApiResponse.success(
            "Candidate created successfully.",
            recruitmentService.createCandidate(request, actor)
        ));
    }

    @Operation(summary = "POST /api/v1/recruitment/candidates/{candidateId}/stage", description = "Processes POST requests for /api/v1/recruitment/candidates/{candidateId}/stage.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping("/candidates/{candidateId}/stage")
    public ResponseEntity<ApiResponse<CandidateView>> changeStage(
        @PathVariable final String candidateId,
        @Valid @RequestBody final CandidateStageChangeRequest request,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedRecruitmentUser actor = currentUser(httpRequest);
        requireAdmin(actor);
        return ResponseEntity.ok(ApiResponse.success(
            "Candidate stage updated successfully.",
            recruitmentService.changeStage(candidateId, request, actor)
        ));
    }

    @Operation(summary = "GET /api/v1/recruitment/candidates", description = "Processes GET requests for /api/v1/recruitment/candidates.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/candidates")
    public ResponseEntity<ApiResponse<com.nexra.hrms.nexra.common.api.PageResponse<CandidateView>>> candidates(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        @RequestParam(required = false) final String jobId,
        @RequestParam(required = false) final String stage,
        @RequestParam(defaultValue = "0") @Min(0) final int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) final int size,
        final HttpServletRequest httpRequest
    ) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
            page, Math.min(size, 100), org.springframework.data.domain.Sort.by("createdAt").descending()
        );
        return ResponseEntity.ok(ApiResponse.success(
            "Candidates fetched successfully.",
            recruitmentService.listCandidates(tenantCode, jobId, stage, currentUser(httpRequest), pageable)
        ));
    }

    @Operation(summary = "GET /api/v1/recruitment/candidates/{candidateId}/history", description = "Processes GET requests for /api/v1/recruitment/candidates/{candidateId}/history.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/candidates/{candidateId}/history")
    public ResponseEntity<ApiResponse<List<CandidateStageHistoryView>>> history(
        @PathVariable final String candidateId,
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Candidate stage history fetched successfully.",
            recruitmentService.history(tenantCode, candidateId, currentUser(httpRequest))
        ));
    }

    @Operation(summary = "GET /api/v1/recruitment/summary", description = "Processes GET requests for /api/v1/recruitment/summary.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<RecruitmentSummaryView>> summary(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Recruitment summary fetched successfully.",
            recruitmentService.summary(tenantCode, currentUser(httpRequest))
        ));
    }

    private AuthenticatedRecruitmentUser currentUser(final HttpServletRequest request) {
        Object value = request.getAttribute(RecruitmentAuthFilter.ATTR_AUTH_USER);
        if (value instanceof AuthenticatedRecruitmentUser user) {
            return user;
        }
        throw new RecruitmentUnauthorizedException("Missing authenticated recruitment user");
    }

    private void requireAdmin(final AuthenticatedRecruitmentUser actor) {
        if (hasRole(actor, "PLATFORM_ADMIN") || hasRole(actor, "TENANT_ADMIN") || hasRole(actor, "HR_ADMIN")) {
            return;
        }
        throw new RecruitmentForbiddenException("User does not have recruitment administration permission");
    }

    private boolean hasRole(final AuthenticatedRecruitmentUser actor, final String role) {
        return actor.roles().contains(role) || actor.roles().contains("ROLE_" + role);
    }
}
