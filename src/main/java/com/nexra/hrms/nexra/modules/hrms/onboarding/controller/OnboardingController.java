package com.nexra.hrms.nexra.modules.hrms.onboarding.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.modules.hrms.onboarding.dto.request.OnboardingPlanCreateRequest;
import com.nexra.hrms.nexra.modules.hrms.onboarding.dto.request.OnboardingTaskCompleteRequest;
import com.nexra.hrms.nexra.modules.hrms.onboarding.dto.request.OnboardingTaskCreateRequest;
import com.nexra.hrms.nexra.modules.hrms.onboarding.exception.OnboardingForbiddenException;
import com.nexra.hrms.nexra.modules.hrms.onboarding.exception.OnboardingUnauthorizedException;
import com.nexra.hrms.nexra.modules.hrms.onboarding.model.OnboardingPlanView;
import com.nexra.hrms.nexra.modules.hrms.onboarding.model.OnboardingSummaryView;
import com.nexra.hrms.nexra.modules.hrms.onboarding.model.OnboardingTaskView;
import com.nexra.hrms.nexra.modules.hrms.onboarding.security.AuthenticatedOnboardingUser;
import com.nexra.hrms.nexra.modules.hrms.onboarding.security.OnboardingAuthFilter;
import com.nexra.hrms.nexra.modules.hrms.onboarding.service.IOnboardingService;
import com.nexra.hrms.nexra.modules.hrms.employee.validation.TenantCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Exposes tenant-scoped onboarding APIs for plans, tasks, and summary.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Tag(name = "Onboarding", description = "Onboarding APIs.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/onboarding")
@Slf4j
@Validated
public class OnboardingController {

    private final IOnboardingService onboardingService;

    @Operation(summary = "GET /api/v1/onboarding/status", description = "Processes GET requests for /api/v1/onboarding/status.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        return ResponseEntity.ok(ApiResponse.success("onboarding service is available.", Map.of(
            "service", "onboarding",
            "timestamp", Instant.now().toString(),
            "state", "UP"
        )));
    }

    @Operation(summary = "GET /api/v1/onboarding/capabilities", description = "Processes GET requests for /api/v1/onboarding/capabilities.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/capabilities")
    public ResponseEntity<ApiResponse<Map<String, Object>>> capabilities() {
        return ResponseEntity.ok(ApiResponse.success("onboarding capabilities fetched successfully.", Map.of(
            "domains", List.of("plans", "tasks"),
            "auth", "JWT tenant-scoped",
            "storage", "MySQL + Flyway"
        )));
    }

    @Operation(summary = "POST /api/v1/onboarding/plans", description = "Processes POST requests for /api/v1/onboarding/plans.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping("/plans")
    public ResponseEntity<ApiResponse<OnboardingPlanView>> createPlan(
        @Valid @RequestBody final OnboardingPlanCreateRequest request,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedOnboardingUser actor = currentUser(httpRequest);
        requireAdmin(actor);
        return ResponseEntity.ok(ApiResponse.success(
            "Onboarding plan created successfully.",
            onboardingService.createPlan(request, actor)
        ));
    }

    @Operation(summary = "POST /api/v1/onboarding/plans/{planId}/tasks", description = "Processes POST requests for /api/v1/onboarding/plans/{planId}/tasks.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping("/plans/{planId}/tasks")
    public ResponseEntity<ApiResponse<OnboardingTaskView>> addTask(
        @PathVariable final String planId,
        @Valid @RequestBody final OnboardingTaskCreateRequest request,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedOnboardingUser actor = currentUser(httpRequest);
        requireAdmin(actor);
        return ResponseEntity.ok(ApiResponse.success(
            "Onboarding task created successfully.",
            onboardingService.addTask(planId, request, actor)
        ));
    }

    @Operation(summary = "POST /api/v1/onboarding/tasks/{taskId}/complete", description = "Processes POST requests for /api/v1/onboarding/tasks/{taskId}/complete.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<ApiResponse<OnboardingTaskView>> completeTask(
        @PathVariable final String taskId,
        @Valid @RequestBody final OnboardingTaskCompleteRequest request,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedOnboardingUser actor = currentUser(httpRequest);
        requireAdmin(actor);
        return ResponseEntity.ok(ApiResponse.success(
            "Onboarding task completed successfully.",
            onboardingService.completeTask(taskId, request, actor)
        ));
    }

    @Operation(summary = "GET /api/v1/onboarding/plans", description = "Processes GET requests for /api/v1/onboarding/plans.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<OnboardingPlanView>>> plans(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        @RequestParam(required = false) final String employeeId,
        @RequestParam(required = false) final String status,
        final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Onboarding plans fetched successfully.",
            onboardingService.listPlans(tenantCode, employeeId, status, currentUser(httpRequest))
        ));
    }

    @Operation(summary = "GET /api/v1/onboarding/plans/{planId}/tasks", description = "Processes GET requests for /api/v1/onboarding/plans/{planId}/tasks.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/plans/{planId}/tasks")
    public ResponseEntity<ApiResponse<List<OnboardingTaskView>>> tasks(
        @PathVariable final String planId,
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Onboarding tasks fetched successfully.",
            onboardingService.listTasks(tenantCode, planId, currentUser(httpRequest))
        ));
    }

    @Operation(summary = "GET /api/v1/onboarding/summary", description = "Processes GET requests for /api/v1/onboarding/summary.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<OnboardingSummaryView>> summary(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Onboarding summary fetched successfully.",
            onboardingService.summary(tenantCode, currentUser(httpRequest))
        ));
    }

    private AuthenticatedOnboardingUser currentUser(final HttpServletRequest request) {
        Object value = request.getAttribute(OnboardingAuthFilter.ATTR_AUTH_USER);
        if (value instanceof AuthenticatedOnboardingUser user) {
            return user;
        }
        throw new OnboardingUnauthorizedException("Missing authenticated onboarding user");
    }

    private void requireAdmin(final AuthenticatedOnboardingUser actor) {
        if (hasRole(actor, "PLATFORM_ADMIN") || hasRole(actor, "TENANT_ADMIN") || hasRole(actor, "HR_ADMIN")
            || hasRole(actor, "ONBOARDING_ADMIN")) {
            return;
        }
        throw new OnboardingForbiddenException("User does not have onboarding administration permission");
    }

    private boolean hasRole(final AuthenticatedOnboardingUser actor, final String role) {
        return actor.roles().contains(role) || actor.roles().contains("ROLE_" + role);
    }
}
