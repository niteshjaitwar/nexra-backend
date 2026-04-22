package com.nexra.hrms.nexra.modules.hrms.leave.repository;

import com.nexra.hrms.nexra.modules.hrms.leave.entity.LeaveRequestEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequestEntity, String> {
    Optional<LeaveRequestEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);
    List<LeaveRequestEntity> findByTenantCodeIgnoreCaseOrderByCreatedAtDesc(String tenantCode);
    List<LeaveRequestEntity> findByTenantCodeIgnoreCaseAndEmployeeIdOrderByCreatedAtDesc(String tenantCode, String employeeId);
}

