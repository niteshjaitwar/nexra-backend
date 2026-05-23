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
 * Exposes tenant-scoped performance APIs for goals, reviews, and summary.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Tag(name = "Performance", description = "Performance APIs.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/performance")
@Validated
public class PerformanceController {

    private final IPerformanceService performanceService;

    @Operation(summary = "GET /api/v1/performance/status", description = "Processes GET requests for /api/v1/performance/status.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        return ResponseEntity.ok(ApiResponse.success("performance service is available.", Map.of(
            "service", "performance",
            "timestamp", Instant.now().toString(),
            "state", "UP"
        )));
    }

    @Operation(summary = "GET /api/v1/performance/capabilities", description = "Processes GET requests for /api/v1/performance/capabilities.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/capabilities")
    public ResponseEntity<ApiResponse<Map<String, Object>>> capabilities() {
        return ResponseEntity.ok(ApiResponse.success("performance capabilities fetched successfully.", Map.of(
            "domains", List.of("goals", "reviews"),
            "auth", "JWT tenant-scoped",
            "storage", "MySQL + Flyway"
        )));
    }

    @Operation(summary = "PUT /api/v1/performance/goals", description = "Processes PUT requests for /api/v1/performance/goals.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
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

    @Operation(summary = "GET /api/v1/performance/goals", description = "Processes GET requests for /api/v1/performance/goals.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/goals")
    public ResponseEntity<ApiResponse<com.nexra.hrms.nexra.common.api.PageResponse<GoalView>>> listGoals(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        @RequestParam(required = false) final String employeeId,
        @RequestParam(required = false) final String status,
        @RequestParam(defaultValue = "0") @Min(0) final int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) final int size,
        final HttpServletRequest httpRequest
    ) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
            page, Math.min(size, 100), org.springframework.data.domain.Sort.by("createdAt").descending()
        );
        return ResponseEntity.ok(ApiResponse.success(
            "Goals fetched successfully.",
            performanceService.listGoals(tenantCode, employeeId, status, currentUser(httpRequest), pageable)
        ));
    }

    @Operation(summary = "POST /api/v1/performance/reviews", description = "Processes POST requests for /api/v1/performance/reviews.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
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

    @Operation(summary = "POST /api/v1/performance/reviews/{reviewId}/complete", description = "Processes POST requests for /api/v1/performance/reviews/{reviewId}/complete.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
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

    @Operation(summary = "GET /api/v1/performance/reviews", description = "Processes GET requests for /api/v1/performance/reviews.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<com.nexra.hrms.nexra.common.api.PageResponse<ReviewView>>> listReviews(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        @RequestParam(required = false) final String employeeId,
        @RequestParam(required = false) final String status,
        @RequestParam(defaultValue = "0") @Min(0) final int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) final int size,
        final HttpServletRequest httpRequest
    ) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
            page, Math.min(size, 100), org.springframework.data.domain.Sort.by("createdAt").descending()
        );
        return ResponseEntity.ok(ApiResponse.success(
            "Reviews fetched successfully.",
            performanceService.listReviews(tenantCode, employeeId, status, currentUser(httpRequest), pageable)
        ));
    }

    @Operation(summary = "GET /api/v1/performance/summary", description = "Processes GET requests for /api/v1/performance/summary.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
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
