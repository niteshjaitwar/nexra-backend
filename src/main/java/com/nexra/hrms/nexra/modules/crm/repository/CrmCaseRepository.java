package com.nexra.hrms.nexra.modules.crm.repository;

import com.nexra.hrms.nexra.modules.crm.entity.CrmCaseEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrmCaseRepository extends JpaRepository<CrmCaseEntity, String> {

    Optional<CrmCaseEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);

    Page<CrmCaseEntity> findAllByTenantCodeIgnoreCaseOrderByUpdatedAtDescIdDesc(String tenantCode, Pageable pageable);
}
