package com.nexra.hrms.nexra.modules.crm.repository;

import com.nexra.hrms.nexra.modules.crm.entity.CrmActivityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrmActivityRepository extends JpaRepository<CrmActivityEntity, String> {

    boolean existsByTenantCodeIgnoreCaseAndLeadIdAndActivityTypeIgnoreCase(String tenantCode, String leadId, String activityType);

    Page<CrmActivityEntity> findAllByTenantCodeIgnoreCaseOrderByOccurredAtDescIdDesc(String tenantCode, Pageable pageable);

    Page<CrmActivityEntity> findAllByTenantCodeIgnoreCaseAndLeadIdOrderByOccurredAtDescIdDesc(
        String tenantCode,
        String leadId,
        Pageable pageable
    );

    Page<CrmActivityEntity> findAllByTenantCodeIgnoreCaseAndContactIdOrderByOccurredAtDescIdDesc(
        String tenantCode,
        String contactId,
        Pageable pageable
    );

    Page<CrmActivityEntity> findAllByTenantCodeIgnoreCaseAndDealIdOrderByOccurredAtDescIdDesc(
        String tenantCode,
        String dealId,
        Pageable pageable
    );
}
