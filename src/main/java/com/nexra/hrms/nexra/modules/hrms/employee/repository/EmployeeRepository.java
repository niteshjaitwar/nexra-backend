package com.nexra.hrms.nexra.modules.hrms.employee.repository;

import com.nexra.hrms.nexra.modules.hrms.employee.entity.EmployeeEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, String> {
    List<EmployeeEntity> findByTenantCodeIgnoreCaseOrderByEmployeeCodeAsc(String tenantCode);
    List<EmployeeEntity> findByTenantCodeIgnoreCaseAndDepartmentIdOrderByEmployeeCodeAsc(String tenantCode, String departmentId);
    Optional<EmployeeEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);
    Optional<EmployeeEntity> findByTenantCodeIgnoreCaseAndEmployeeCodeIgnoreCase(String tenantCode, String employeeCode);
    Optional<EmployeeEntity> findByTenantCodeIgnoreCaseAndWorkEmailIgnoreCase(String tenantCode, String workEmail);
    long countByTenantCodeIgnoreCaseAndActiveTrue(String tenantCode);
}
