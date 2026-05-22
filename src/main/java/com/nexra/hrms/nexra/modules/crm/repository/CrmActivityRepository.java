package com.nexra.hrms.nexra.modules.crm.repository;

import com.nexra.hrms.nexra.modules.crm.entity.CrmActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrmActivityRepository extends JpaRepository<CrmActivityEntity, String> {

    boolean existsByTenantCodeIgnoreCaseAndLeadIdAndActivityTypeIgnoreCase(String tenantCode, String leadId, String activityType);
}

