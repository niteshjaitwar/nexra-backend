package com.nexra.hrms.nexra.modules.hrms.performance.repository;

import com.nexra.hrms.nexra.modules.hrms.performance.entity.GoalEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<GoalEntity, String> {
    Optional<GoalEntity> findByTenantCodeAndGoalId(String tenantCode, String goalId);
    List<GoalEntity> findByTenantCodeOrderByCreatedAtDesc(String tenantCode);
    long countByTenantCode(String tenantCode);
    long countByTenantCodeAndStatus(String tenantCode, String status);

    // Paginated queries
    Page<GoalEntity> findByTenantCode(String tenantCode, Pageable pageable);
    Page<GoalEntity> findByTenantCodeAndEmployeeId(String tenantCode, String employeeId, Pageable pageable);
    Page<GoalEntity> findByTenantCodeAndStatusIgnoreCase(String tenantCode, String status, Pageable pageable);
    Page<GoalEntity> findByTenantCodeAndEmployeeIdAndStatusIgnoreCase(String tenantCode, String employeeId, String status, Pageable pageable);
}
