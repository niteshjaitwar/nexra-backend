package com.nexra.hrms.nexra.modules.payroll.repository;

import com.nexra.hrms.nexra.modules.payroll.entity.PayrollStatutorySlabEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollStatutorySlabRepository extends JpaRepository<PayrollStatutorySlabEntity, String> {

    List<PayrollStatutorySlabEntity> findAllByTenantCodeIgnoreCaseAndCountryCodeIgnoreCaseAndComponentCodeIgnoreCaseAndActiveTrue(
        String tenantCode,
        String countryCode,
        String componentCode
    );
}
