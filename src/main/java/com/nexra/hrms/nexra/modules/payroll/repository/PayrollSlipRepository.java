package com.nexra.hrms.nexra.modules.payroll.repository;

import com.nexra.hrms.nexra.modules.payroll.entity.PayrollSlipEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollSlipRepository extends JpaRepository<PayrollSlipEntity, String> {

    List<PayrollSlipEntity> findByTenantCodeIgnoreCaseOrderByGeneratedAtDesc(String tenantCode);

    long countByTenantCodeIgnoreCase(String tenantCode);
}
