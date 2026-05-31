package com.nexra.hrms.nexra.modules.hrms.attendance.repository;

import com.nexra.hrms.nexra.modules.hrms.attendance.entity.AttendanceRegularizationEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRegularizationRepository extends JpaRepository<AttendanceRegularizationEntity, String> {

    Optional<AttendanceRegularizationEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);

    boolean existsByTenantCodeIgnoreCaseAndEmployeeIdAndWorkDateAndStatusIgnoreCase(
        String tenantCode,
        String employeeId,
        java.time.LocalDate workDate,
        String status
    );
}
