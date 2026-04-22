package com.nexra.hrms.nexra.modules.hrms.expense.repository;

import com.nexra.hrms.nexra.modules.hrms.expense.entity.ExpenseCategoryEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategoryEntity, String> {
    Optional<ExpenseCategoryEntity> findByTenantCodeIgnoreCaseAndCodeIgnoreCase(String tenantCode, String code);
    List<ExpenseCategoryEntity> findByTenantCodeIgnoreCaseOrderByCodeAsc(String tenantCode);
}

