package com.nexra.hrms.nexra.modules.hrms.timesheet.repository;

import com.nexra.hrms.nexra.modules.hrms.timesheet.entity.TimesheetEntryEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimesheetEntryRepository extends JpaRepository<TimesheetEntryEntity, String> {
    Optional<TimesheetEntryEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);
    List<TimesheetEntryEntity> findByTenantCodeIgnoreCaseOrderByCreatedAtDesc(String tenantCode);
    List<TimesheetEntryEntity> findByTenantCodeIgnoreCaseAndEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(
        String tenantCode, String employeeId, LocalDate fromDate, LocalDate toDate
    );

    // Paginated queries
    Page<TimesheetEntryEntity> findByTenantCodeIgnoreCaseAndEmployeeIdAndWorkDateBetween(
        String tenantCode, String employeeId, LocalDate fromDate, LocalDate toDate, Pageable pageable
    );
    Page<TimesheetEntryEntity> findByTenantCodeIgnoreCase(String tenantCode, Pageable pageable);
}
