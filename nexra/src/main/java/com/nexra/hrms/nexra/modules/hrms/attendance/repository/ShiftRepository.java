package com.nexra.hrms.nexra.modules.hrms.attendance.repository;

import com.nexra.hrms.nexra.modules.hrms.attendance.entity.ShiftEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftRepository extends JpaRepository<ShiftEntity, String> {
    List<ShiftEntity> findByTenantCodeIgnoreCaseOrderByCodeAsc(String tenantCode);
    Optional<ShiftEntity> findByTenantCodeIgnoreCaseAndCodeIgnoreCase(String tenantCode, String code);
}

