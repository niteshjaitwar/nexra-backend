package com.nexra.hrms.nexra.modules.hrms.performance.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.modules.hrms.performance.dto.request.GoalUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.performance.dto.request.ReviewCompleteRequest;
import com.nexra.hrms.nexra.modules.hrms.performance.dto.request.ReviewCreateRequest;
import com.nexra.hrms.nexra.modules.hrms.performance.exception.PerformanceForbiddenException;
import com.nexra.hrms.nexra.modules.hrms.performance.exception.PerformanceUnauthorizedException;
import com.nexra.hrms.nexra.modules.hrms.performance.model.GoalView;
import com.nexra.hrms.nexra.modules.hrms.performance.model.PerformanceSummaryView;
import com.nexra.hrms.nexra.modules.hrms.performance.model.ReviewView;
import com.nexra.hrms.nexra.modules.hrms.performance.security.AuthenticatedPerformanceUser;
import com.nexra.hrms.nexra.modules.hrms.performance.security.PerformanceAuthFilter;
import com.nexra.hrms.nexra.modules.hrms.performance.service.IPerformanceService;
import com.nexra.hrms.nexra.modules.hrms.employee.validation.TenantCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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

/**
 * Exposes tenant-scoped performance APIs for goals, reviews, and summary.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/performance")
@Validated
public class PerformanceController {

    private final IPerformanceService performanceService;

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        return ResponseEntity.ok(ApiResponse.success("performance service is available.", Map.of(
            "service", "performance",
            "timestamp", Instant.now().toString(),
            "state", "UP"
        )));
    }

    @GetMapping("/capabilities")
    public ResponseEntity<ApiResponse<Map<String, Object>>> capabilities() {
        return ResponseEntity.ok(ApiResponse.success("performance capabilities fetched successfully.", Map.of(
            "domains", List.of("goals", "reviews"),
            "auth", "JWT tenant-scoped",
            "storage", "MySQL + Flyway"
        )));
    }

    @PutMapping("/goals")
    public ResponseEntity<ApiResponse<GoalView>> upsertGoal(
        @Valid @RequestBody final GoalUpsertRequest request,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedPerformanceUser actor = currentUser(httpRequest);
        requireAdmin(actor);
        return ResponseEntity.ok(ApiResponse.success(
            "Goal upserted successfully.",
            performanceService.upsertGoal(request, actor)
        ));
    }

    @GetMapping("/goals")
    public ResponseEntity<ApiResponse<List<GoalView>>> listGoals(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        @RequestParam(required = false) final String employeeId,
        @RequestParam(required = false) final String status,
        final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Goals fetched successfully.",
            performanceService.listGoals(tenantCode, employeeId, status, currentUser(httpRequest))
        ));
    }

    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<ReviewView>> createReview(
        @Valid @RequestBody final ReviewCreateRequest request,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedPerformanceUser actor = currentUser(httpRequest);
        requireAdmin(actor);
        return ResponseEntity.ok(ApiResponse.success(
            "Review created successfully.",
            performanceService.createReview(request, actor)
        ));
    }

    @PostMapping("/reviews/{reviewId}/complete")
    public ResponseEntity<ApiResponse<ReviewView>> completeReview(
        @PathVariable final String reviewId,
        @Valid @RequestBody final ReviewCompleteRequest request,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedPerformanceUser actor = currentUser(httpRequest);
        requireAdmin(actor);
        return ResponseEntity.ok(ApiResponse.success(
            "Review completed successfully.",
            performanceService.completeReview(reviewId, request, actor)
        ));
    }

    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<List<ReviewView>>> listReviews(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        @RequestParam(required = false) final String employeeId,
        @RequestParam(required = false) final String status,
        final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Reviews fetched successfully.",
            performanceService.listReviews(tenantCode, employeeId, status, currentUser(httpRequest))
        ));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<PerformanceSummaryView>> summary(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Performance summary fetched successfully.",
            performanceService.summary(tenantCode, currentUser(httpRequest))
        ));
    }

    private AuthenticatedPerformanceUser currentUser(final HttpServletRequest request) {
        Object value = request.getAttribute(PerformanceAuthFilter.ATTR_AUTH_USER);
        if (value instanceof AuthenticatedPerformanceUser user) {
            return user;
        }
        throw new PerformanceUnauthorizedException("Missing authenticated performance user");
    }

    private void requireAdmin(final AuthenticatedPerformanceUser actor) {
        if (hasRole(actor, "PLATFORM_ADMIN") || hasRole(actor, "TENANT_ADMIN") || hasRole(actor, "HR_ADMIN")) {
            return;
        }
        throw new PerformanceForbiddenException("User does not have performance administration permission");
    }

    private boolean hasRole(final AuthenticatedPerformanceUser actor, final String role) {
        return actor.roles().contains(role) || actor.roles().contains("ROLE_" + role);
    }
}
