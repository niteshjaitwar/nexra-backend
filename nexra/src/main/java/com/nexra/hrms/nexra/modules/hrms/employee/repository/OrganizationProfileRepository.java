package com.nexra.hrms.nexra.modules.hrms.employee.repository;

import com.nexra.hrms.nexra.modules.hrms.employee.entity.OrganizationProfileEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationProfileRepository extends JpaRepository<OrganizationProfileEntity, String> {
    Optional<OrganizationProfileEntity> findByTenantCodeIgnoreCase(String tenantCode);
}
