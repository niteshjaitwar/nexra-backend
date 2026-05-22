package com.nexra.hrms.nexra.modules.crm.repository;

import com.nexra.hrms.nexra.modules.crm.entity.CrmLeadEntity;
import com.nexra.hrms.nexra.modules.crm.model.CrmLeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CrmLeadRepository extends JpaRepository<CrmLeadEntity, String> {

    Optional<CrmLeadEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);

    Page<CrmLeadEntity> findAllByTenantCodeIgnoreCaseOrderByDomainUpdatedAtDescIdDesc(String tenantCode, Pageable pageable);

    boolean existsByTenantCodeIgnoreCaseAndEmailIgnoreCase(String tenantCode, String email);

    boolean existsByTenantCodeIgnoreCaseAndEmailIgnoreCaseAndIdNot(String tenantCode, String email, String id);

    long countByTenantCodeIgnoreCase(String tenantCode);

    long countByTenantCodeIgnoreCaseAndStatus(String tenantCode, CrmLeadStatus status);
}
