package com.nexra.hrms.nexra.modules.crm.repository;

import com.nexra.hrms.nexra.modules.crm.entity.CrmCampaignEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrmCampaignRepository extends JpaRepository<CrmCampaignEntity, String> {

    Optional<CrmCampaignEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);

    Page<CrmCampaignEntity> findAllByTenantCodeIgnoreCaseOrderByUpdatedAtDescIdDesc(String tenantCode, Pageable pageable);
}
