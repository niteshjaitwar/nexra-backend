package com.nexra.hrms.nexra.modules.crm.repository;

import com.nexra.hrms.nexra.modules.crm.entity.CrmWorkflowRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrmWorkflowRuleRepository extends JpaRepository<CrmWorkflowRuleEntity, String> {

    List<CrmWorkflowRuleEntity> findAllByTenantCodeIgnoreCaseAndModuleKeyIgnoreCaseOrderByPriorityAscNameAsc(
        String tenantCode,
        String moduleKey
    );
}
