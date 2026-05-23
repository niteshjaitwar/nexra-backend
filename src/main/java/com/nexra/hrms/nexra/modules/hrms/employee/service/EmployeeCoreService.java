package com.nexra.hrms.nexra.modules.hrms.employee.service;

import com.nexra.hrms.nexra.modules.hrms.employee.dto.request.DepartmentUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.employee.dto.request.EmployeeUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.employee.dto.request.OrganizationProfileUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.employee.model.Department;
import com.nexra.hrms.nexra.modules.hrms.employee.model.Employee;
import com.nexra.hrms.nexra.modules.hrms.employee.model.OrganizationProfile;
import com.nexra.hrms.nexra.modules.hrms.employee.security.AuthenticatedEmployeeCoreUser;
import com.nexra.hrms.nexra.common.api.PageResponse;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;

/**
 * Defines tenant-scoped core HR employee master operations.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public interface EmployeeCoreService {

    OrganizationProfile upsertOrganizationProfile(OrganizationProfileUpsertRequest request, AuthenticatedEmployeeCoreUser actor);

    OrganizationProfile getOrganizationProfile(String tenantCode, AuthenticatedEmployeeCoreUser actor);

    Department upsertDepartment(DepartmentUpsertRequest request, AuthenticatedEmployeeCoreUser actor);

    List<Department> listDepartments(String tenantCode, boolean includeInactive, AuthenticatedEmployeeCoreUser actor);

    Department getDepartment(String tenantCode, String departmentId, AuthenticatedEmployeeCoreUser actor);

    Employee upsertEmployee(EmployeeUpsertRequest request, AuthenticatedEmployeeCoreUser actor);

    /** Paginated employee listing with optional department filter. */
    PageResponse<Employee> listEmployees(String tenantCode, String departmentId, boolean includeInactive, AuthenticatedEmployeeCoreUser actor, Pageable pageable);

    Employee getEmployee(String tenantCode, String employeeId, AuthenticatedEmployeeCoreUser actor);

    Map<String, Object> summary(String tenantCode, AuthenticatedEmployeeCoreUser actor);
}

