package com.nexra.hrms.nexra.modules.hrms.leave.repository;

import com.nexra.hrms.nexra.modules.hrms.leave.entity.LeaveRequestEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequestEntity, String> {
    Optional<LeaveRequestEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);
    List<LeaveRequestEntity> findByTenantCodeIgnoreCaseOrderByCreatedAtDesc(String tenantCode);
    List<LeaveRequestEntity> findByTenantCodeIgnoreCaseAndEmployeeIdOrderByCreatedAtDesc(String tenantCode, String employeeId);

    // Paginated queries
    Page<LeaveRequestEntity> findByTenantCodeIgnoreCase(String tenantCode, Pageable pageable);
    Page<LeaveRequestEntity> findByTenantCodeIgnoreCaseAndEmployeeId(String tenantCode, String employeeId, Pageable pageable);
    Page<LeaveRequestEntity> findByTenantCodeIgnoreCaseAndStatusIgnoreCase(String tenantCode, String status, Pageable pageable);
    Page<LeaveRequestEntity> findByTenantCodeIgnoreCaseAndEmployeeIdAndStatusIgnoreCase(String tenantCode, String employeeId, String status, Pageable pageable);

    long countByTenantCodeIgnoreCase(String tenantCode);

    long countByTenantCodeIgnoreCaseAndStatusIn(String tenantCode, Collection<String> statuses);
}
