package com.nexra.hrms.nexra.modules.crm.repository;

import com.nexra.hrms.nexra.modules.crm.entity.CrmContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrmContactRepository extends JpaRepository<CrmContactEntity, String> {
}

