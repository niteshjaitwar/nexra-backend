package com.nexra.hrms.nexra.modules.crm.repository;

import com.nexra.hrms.nexra.modules.crm.entity.CrmAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrmAccountRepository extends JpaRepository<CrmAccountEntity, String> {
}

