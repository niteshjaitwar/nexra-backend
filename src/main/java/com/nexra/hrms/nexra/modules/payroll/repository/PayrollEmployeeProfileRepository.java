package com.nexra.hrms.nexra.modules.payroll.repository;

import com.nexra.hrms.nexra.modules.payroll.entity.PayrollEmployeeProfileEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollEmployeeProfileRepository extends JpaRepository<PayrollEmployeeProfileEntity, String> {

    Optional<PayrollEmployeeProfileEntity> findByTenantCodeIgnoreCaseAndEmployeeId(String tenantCode, String employeeId);

    List<PayrollEmployeeProfileEntity> findByTenantCodeIgnoreCaseOrderByEmployeeCodeAsc(String tenantCode);
}
