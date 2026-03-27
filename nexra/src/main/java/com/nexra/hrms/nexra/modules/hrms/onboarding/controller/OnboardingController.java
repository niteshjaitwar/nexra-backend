package com.nexra.hrms.nexra.modules.hrms.onboarding.controller;

import com.nexra.hrms.nexra.modules.hrms.onboarding.dto.ApiResponse;
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
import com.nexra.hrms.nexra.modules.hrms.onboarding.service.OnboardingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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

/**
 * Exposes tenant-scoped onboarding APIs for plans, tasks, and summary.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/onboarding")
@Slf4j
public class OnboardingController {

    private final OnboardingService onboardingService;

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        return ResponseEntity.ok(ApiResponse.success("onboarding service is available.", Map.of(
            "service", "onboarding",
            "timestamp", Instant.now().toString(),
            "state", "UP"
        )));
    }

    @GetMapping("/capabilities")
    public ResponseEntity<ApiResponse<Map<String, Object>>> capabilities() {
        return ResponseEntity.ok(ApiResponse.success("onboarding capabilities fetched successfully.", Map.of(
            "domains", List.of("plans", "tasks"),
            "auth", "JWT tenant-scoped",
            "storage", "MySQL + Flyway"
        )));
    }

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

    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<OnboardingPlanView>>> plans(
        @RequestParam final String tenantCode,
        @RequestParam(required = false) final String employeeId,
        @RequestParam(required = false) final String status,
        final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Onboarding plans fetched successfully.",
            onboardingService.listPlans(tenantCode, employeeId, status, currentUser(httpRequest))
        ));
    }

    @GetMapping("/plans/{planId}/tasks")
    public ResponseEntity<ApiResponse<List<OnboardingTaskView>>> tasks(
        @PathVariable final String planId,
        @RequestParam final String tenantCode,
        final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Onboarding tasks fetched successfully.",
            onboardingService.listTasks(tenantCode, planId, currentUser(httpRequest))
        ));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<OnboardingSummaryView>> summary(
        @RequestParam final String tenantCode,
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
        if (hasRole(actor, "PLATFORM_ADMIN") || hasRole(actor, "TENANT_ADMIN") || hasRole(actor, "HR_ADMIN")) {
            return;
        }
        throw new OnboardingForbiddenException("User does not have onboarding administration permission");
    }

    private boolean hasRole(final AuthenticatedOnboardingUser actor, final String role) {
        return actor.roles().contains(role) || actor.roles().contains("ROLE_" + role);
    }
}
