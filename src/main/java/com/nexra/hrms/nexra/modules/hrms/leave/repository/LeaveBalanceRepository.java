package com.nexra.hrms.nexra.modules.hrms.leave.repository;

import com.nexra.hrms.nexra.modules.hrms.leave.entity.LeaveBalanceEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalanceEntity, String> {
    Optional<LeaveBalanceEntity> findByTenantCodeIgnoreCaseAndEmployeeIdAndLeaveTypeCodeIgnoreCase(
        String tenantCode, String employeeId, String leaveTypeCode
    );
    List<LeaveBalanceEntity> findByTenantCodeIgnoreCaseAndEmployeeIdOrderByLeaveTypeCodeAsc(String tenantCode, String employeeId);
}

