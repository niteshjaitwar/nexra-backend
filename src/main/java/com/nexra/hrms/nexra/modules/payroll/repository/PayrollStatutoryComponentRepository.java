package com.nexra.hrms.nexra.modules.payroll.repository;

import com.nexra.hrms.nexra.modules.payroll.entity.PayrollStatutoryComponentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollStatutoryComponentRepository extends JpaRepository<PayrollStatutoryComponentEntity, String> {

    List<PayrollStatutoryComponentEntity> findAllByTenantCodeIgnoreCaseAndCountryCodeIgnoreCaseAndActiveTrueOrderByComponentCodeAsc(
        String tenantCode,
        String countryCode
    );
}
