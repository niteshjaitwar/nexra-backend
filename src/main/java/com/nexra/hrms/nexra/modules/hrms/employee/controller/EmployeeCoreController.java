package com.nexra.hrms.nexra.modules.hrms.employee.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.modules.hrms.employee.dto.request.DepartmentUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.employee.dto.request.EmployeeUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.employee.dto.request.OrganizationProfileUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.employee.exception.EmployeeCoreForbiddenException;
import com.nexra.hrms.nexra.modules.hrms.employee.exception.EmployeeCoreUnauthorizedException;
import com.nexra.hrms.nexra.modules.hrms.employee.model.Department;
import com.nexra.hrms.nexra.modules.hrms.employee.model.Employee;
import com.nexra.hrms.nexra.modules.hrms.employee.model.OrganizationProfile;
import com.nexra.hrms.nexra.modules.hrms.employee.security.AuthenticatedEmployeeCoreUser;
import com.nexra.hrms.nexra.modules.hrms.employee.security.EmployeeCoreAuthFilter;
import com.nexra.hrms.nexra.modules.hrms.employee.service.EmployeeCoreService;
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
import lombok.extern.slf4j.Slf4j;
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
 * Exposes tenant-scoped employee-core APIs for org profile, departments, employees, and summary.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Tag(name = "Employee Core", description = "Employee core APIs.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/employee-core")
@Slf4j
@Validated
public class EmployeeCoreController {

    private final EmployeeCoreService employeeCoreService;

    @Operation(summary = "GET /api/v1/employee-core/status", description = "Processes GET requests for /api/v1/employee-core/status.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        return ResponseEntity.ok(ApiResponse.success(
            "employee-core service is available.",
            Map.of(
                "service", "employee-core",
                "timestamp", Instant.now().toString(),
                "state", "UP"
            )
        ));
    }

    @Operation(summary = "GET /api/v1/employee-core/capabilities", description = "Processes GET requests for /api/v1/employee-core/capabilities.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/capabilities")
    public ResponseEntity<ApiResponse<Map<String, Object>>> capabilities() {
        return ResponseEntity.ok(ApiResponse.success(
            "employee-core capabilities fetched successfully.",
            Map.of(
                "domains", List.of("organization-profile", "departments", "employees", "summary"),
                "auth", "JWT tenant-scoped (same claim format as payroll)",
                "storage", "MySQL + Flyway"
            )
        ));
    }

    @Operation(summary = "PUT /api/v1/employee-core/organization-profile", description = "Processes PUT requests for /api/v1/employee-core/organization-profile.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PutMapping("/organization-profile")
    public ResponseEntity<ApiResponse<OrganizationProfile>> upsertOrganizationProfile(
        @Valid @RequestBody final OrganizationProfileUpsertRequest request,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedEmployeeCoreUser actor = currentUser(httpRequest);
        requireHrAdmin(actor);
        log.info("EmployeeCoreController - upsertOrganizationProfile - tenantCode={}", request.tenantCode());
        return ResponseEntity.ok(ApiResponse.success(
            "Organization profile saved successfully.",
            employeeCoreService.upsertOrganizationProfile(request, actor)
        ));
    }

    @Operation(summary = "GET /api/v1/employee-core/organization-profile", description = "Processes GET requests for /api/v1/employee-core/organization-profile.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/organization-profile")
    public ResponseEntity<ApiResponse<OrganizationProfile>> getOrganizationProfile(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Organization profile fetched successfully.",
            employeeCoreService.getOrganizationProfile(tenantCode, currentUser(httpRequest))
        ));
    }

    @Operation(summary = "POST /api/v1/employee-core/departments", description = "Processes POST requests for /api/v1/employee-core/departments.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping("/departments")
    public ResponseEntity<ApiResponse<Department>> createDepartment(
        @Valid @RequestBody final DepartmentUpsertRequest request,
        final HttpServletRequest httpRequest
    ) {
        return upsertDepartment(request, httpRequest);
    }

    @Operation(summary = "PUT /api/v1/employee-core/departments", description = "Processes PUT requests for /api/v1/employee-core/departments.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PutMapping("/departments")
    public ResponseEntity<ApiResponse<Department>> upsertDepartment(
        @Valid @RequestBody final DepartmentUpsertRequest request,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedEmployeeCoreUser actor = currentUser(httpRequest);
        requireHrAdmin(actor);
        log.info("EmployeeCoreController - upsertDepartment - tenantCode={}, code={}", request.tenantCode(), request.code());
        return ResponseEntity.ok(ApiResponse.success(
            "Department saved successfully.",
            employeeCoreService.upsertDepartment(request, actor)
        ));
    }

    @Operation(summary = "GET /api/v1/employee-core/departments", description = "Processes GET requests for /api/v1/employee-core/departments.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<Department>>> listDepartments(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        @RequestParam(defaultValue = "false") final boolean includeInactive,
        final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Departments fetched successfully.",
            employeeCoreService.listDepartments(tenantCode, includeInactive, currentUser(httpRequest))
        ));
    }

    @Operation(summary = "GET /api/v1/employee-core/departments/{departmentId}", description = "Processes GET requests for /api/v1/employee-core/departments/{departmentId}.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/departments/{departmentId}")
    public ResponseEntity<ApiResponse<Department>> getDepartment(
        @PathVariable @NotBlank @Size(max = 36) final String departmentId,
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Department fetched successfully.",
            employeeCoreService.getDepartment(tenantCode, departmentId, currentUser(httpRequest))
        ));
    }

    @Operation(summary = "POST /api/v1/employee-core/employees", description = "Processes POST requests for /api/v1/employee-core/employees.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping("/employees")
    public ResponseEntity<ApiResponse<Employee>> createEmployee(
        @Valid @RequestBody final EmployeeUpsertRequest request,
        final HttpServletRequest httpRequest
    ) {
        return upsertEmployee(request, httpRequest);
    }

    @Operation(summary = "PUT /api/v1/employee-core/employees", description = "Processes PUT requests for /api/v1/employee-core/employees.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PutMapping("/employees")
    public ResponseEntity<ApiResponse<Employee>> upsertEmployee(
        @Valid @RequestBody final EmployeeUpsertRequest request,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedEmployeeCoreUser actor = currentUser(httpRequest);
        requireHrAdmin(actor);
        log.info("EmployeeCoreController - upsertEmployee - tenantCode={}, employeeCode={}", request.tenantCode(), request.employeeCode());
        return ResponseEntity.ok(ApiResponse.success(
            "Employee profile saved successfully.",
            employeeCoreService.upsertEmployee(request, actor)
        ));
    }

    @Operation(summary = "GET /api/v1/employee-core/employees", description = "Processes GET requests for /api/v1/employee-core/employees.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/employees")
    public ResponseEntity<ApiResponse<com.nexra.hrms.nexra.common.api.PageResponse<Employee>>> listEmployees(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        @RequestParam(required = false) @Size(max = 36) final String departmentId,
        @RequestParam(defaultValue = "false") final boolean includeInactive,
        @RequestParam(defaultValue = "0") @Min(0) final int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) final int size,
        final HttpServletRequest httpRequest
    ) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
            page, Math.min(size, 100), org.springframework.data.domain.Sort.by("employeeCode").ascending()
        );
        return ResponseEntity.ok(ApiResponse.success(
            "Employees fetched successfully.",
            employeeCoreService.listEmployees(tenantCode, departmentId, includeInactive, currentUser(httpRequest), pageable)
        ));
    }

    @Operation(summary = "GET /api/v1/employee-core/employees/{employeeId}", description = "Processes GET requests for /api/v1/employee-core/employees/{employeeId}.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<ApiResponse<Employee>> getEmployee(
        @PathVariable @NotBlank @Size(max = 36) final String employeeId,
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Employee fetched successfully.",
            employeeCoreService.getEmployee(tenantCode, employeeId, currentUser(httpRequest))
        ));
    }

    @Operation(summary = "GET /api/v1/employee-core/summary", description = "Processes GET requests for /api/v1/employee-core/summary.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> summary(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Employee-core summary fetched successfully.",
            employeeCoreService.summary(tenantCode, currentUser(httpRequest))
        ));
    }

    private AuthenticatedEmployeeCoreUser currentUser(final HttpServletRequest request) {
        Object value = request.getAttribute(EmployeeCoreAuthFilter.ATTR_AUTH_USER);
        if (value instanceof AuthenticatedEmployeeCoreUser user) {
            return user;
        }
        throw new EmployeeCoreUnauthorizedException("Missing authenticated employee-core user");
    }

    private void requireHrAdmin(final AuthenticatedEmployeeCoreUser actor) {
        if (hasRole(actor, "PLATFORM_ADMIN") || hasRole(actor, "TENANT_ADMIN") || hasRole(actor, "HR_ADMIN")) {
            return;
        }
        throw new EmployeeCoreForbiddenException("User does not have employee-core administration permission");
    }

    private boolean hasRole(final AuthenticatedEmployeeCoreUser actor, final String role) {
        return actor.roles().contains(role) || actor.roles().contains("ROLE_" + role);
    }
}
