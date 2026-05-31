package com.nexra.hrms.nexra.modules.hrms.attendance.repository;

import com.nexra.hrms.nexra.modules.hrms.attendance.entity.AttendanceRecordEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecordEntity, String> {
    Optional<AttendanceRecordEntity> findByTenantCodeIgnoreCaseAndEmployeeIdAndWorkDate(String tenantCode, String employeeId, LocalDate workDate);
    List<AttendanceRecordEntity> findByTenantCodeIgnoreCaseAndWorkDateBetweenOrderByWorkDateAsc(String tenantCode, LocalDate fromDate, LocalDate toDate);
    List<AttendanceRecordEntity> findByTenantCodeIgnoreCaseAndEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(
        String tenantCode, String employeeId, LocalDate fromDate, LocalDate toDate
    );

    // Paginated queries
    Page<AttendanceRecordEntity> findByTenantCodeIgnoreCaseAndWorkDateBetween(String tenantCode, LocalDate fromDate, LocalDate toDate, Pageable pageable);
    Page<AttendanceRecordEntity> findByTenantCodeIgnoreCaseAndEmployeeIdAndWorkDateBetween(String tenantCode, String employeeId, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    long countByTenantCodeIgnoreCaseAndWorkDateBetween(String tenantCode, LocalDate fromDate, LocalDate toDate);

    long countByTenantCodeIgnoreCaseAndWorkDateBetweenAndCheckOutAtIsNull(
        String tenantCode,
        LocalDate fromDate,
        LocalDate toDate
    );
}
