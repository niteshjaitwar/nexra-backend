package com.nexra.hrms.nexra.modules.payroll.repository;

import com.nexra.hrms.nexra.modules.payroll.entity.PayrollOrganizationProfileEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollOrganizationProfileRepository extends JpaRepository<PayrollOrganizationProfileEntity, String> {

    Optional<PayrollOrganizationProfileEntity> findByTenantCodeIgnoreCase(String tenantCode);
}
