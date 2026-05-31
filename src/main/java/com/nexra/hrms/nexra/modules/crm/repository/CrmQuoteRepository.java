package com.nexra.hrms.nexra.modules.crm.repository;

import com.nexra.hrms.nexra.modules.crm.entity.CrmQuoteEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrmQuoteRepository extends JpaRepository<CrmQuoteEntity, String> {

    Optional<CrmQuoteEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);

    Page<CrmQuoteEntity> findAllByTenantCodeIgnoreCaseOrderByUpdatedAtDescIdDesc(String tenantCode, Pageable pageable);
}
