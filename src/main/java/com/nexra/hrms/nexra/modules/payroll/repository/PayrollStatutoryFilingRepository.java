package com.nexra.hrms.nexra.modules.payroll.repository;

import com.nexra.hrms.nexra.modules.payroll.entity.PayrollStatutoryFilingEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollStatutoryFilingRepository extends JpaRepository<PayrollStatutoryFilingEntity, String> {

    List<PayrollStatutoryFilingEntity> findAllByTenantCodeIgnoreCaseAndCountryCodeIgnoreCaseOrderByPeriodDescGeneratedAtDesc(
        String tenantCode,
        String countryCode
    );

    Optional<PayrollStatutoryFilingEntity> findByTenantCodeIgnoreCaseAndCountryCodeIgnoreCaseAndPeriodAndFilingType(
        String tenantCode,
        String countryCode,
        String period,
        String filingType
    );

    Optional<PayrollStatutoryFilingEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);
}
