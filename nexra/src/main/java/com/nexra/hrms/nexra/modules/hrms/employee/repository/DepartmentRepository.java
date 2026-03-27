package com.nexra.hrms.nexra.modules.hrms.employee.repository;

import com.nexra.hrms.nexra.modules.hrms.employee.entity.DepartmentEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<DepartmentEntity, String> {
    List<DepartmentEntity> findByTenantCodeIgnoreCaseOrderByCodeAsc(String tenantCode);
    Optional<DepartmentEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);
    Optional<DepartmentEntity> findByTenantCodeIgnoreCaseAndCodeIgnoreCase(String tenantCode, String code);
    long countByTenantCodeIgnoreCaseAndActiveTrue(String tenantCode);
}
