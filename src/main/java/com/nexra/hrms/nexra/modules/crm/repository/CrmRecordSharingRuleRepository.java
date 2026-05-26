package com.nexra.hrms.nexra.modules.crm.repository;

import com.nexra.hrms.nexra.modules.crm.entity.CrmRecordSharingRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrmRecordSharingRuleRepository extends JpaRepository<CrmRecordSharingRuleEntity, String> {

    List<CrmRecordSharingRuleEntity> findAllByTenantCodeIgnoreCaseAndModuleKeyIgnoreCaseOrderByNameAsc(
        String tenantCode,
        String moduleKey
    );
}
