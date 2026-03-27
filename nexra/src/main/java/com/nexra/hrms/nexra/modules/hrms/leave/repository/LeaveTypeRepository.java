package com.nexra.hrms.nexra.modules.hrms.leave.repository;

import com.nexra.hrms.nexra.modules.hrms.leave.entity.LeaveTypeEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveTypeRepository extends JpaRepository<LeaveTypeEntity, String> {
    List<LeaveTypeEntity> findByTenantCodeIgnoreCaseOrderByCodeAsc(String tenantCode);
    Optional<LeaveTypeEntity> findByTenantCodeIgnoreCaseAndCodeIgnoreCase(String tenantCode, String code);
}

