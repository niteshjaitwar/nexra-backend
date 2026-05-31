package com.nexra.hrms.nexra.modules.crm.repository;

import com.nexra.hrms.nexra.modules.crm.entity.CrmCustomFieldValueEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrmCustomFieldValueRepository extends JpaRepository<CrmCustomFieldValueEntity, String> {

    List<CrmCustomFieldValueEntity> findAllByTenantCodeIgnoreCaseAndModuleKeyIgnoreCaseAndRecordId(
        String tenantCode,
        String moduleKey,
        String recordId
    );

    Optional<CrmCustomFieldValueEntity> findByTenantCodeIgnoreCaseAndModuleKeyIgnoreCaseAndRecordIdAndFieldKeyIgnoreCase(
        String tenantCode,
        String moduleKey,
        String recordId,
        String fieldKey
    );
}
