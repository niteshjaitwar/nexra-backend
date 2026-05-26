package com.nexra.hrms.nexra.modules.crm.repository;

import com.nexra.hrms.nexra.modules.crm.entity.CrmCustomFieldDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrmCustomFieldDefinitionRepository extends JpaRepository<CrmCustomFieldDefinitionEntity, String> {

    boolean existsByTenantCodeIgnoreCaseAndModuleKeyIgnoreCaseAndFieldKeyIgnoreCase(
        String tenantCode,
        String moduleKey,
        String fieldKey
    );

    List<CrmCustomFieldDefinitionEntity> findAllByTenantCodeIgnoreCaseAndModuleKeyIgnoreCaseOrderByFieldKeyAsc(
        String tenantCode,
        String moduleKey
    );
}
