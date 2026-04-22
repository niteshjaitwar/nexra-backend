package com.nexra.hrms.nexra.modules.hrms.expense.repository;

import com.nexra.hrms.nexra.modules.hrms.expense.entity.ExpenseClaimEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseClaimRepository extends JpaRepository<ExpenseClaimEntity, String> {
    Optional<ExpenseClaimEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);
    List<ExpenseClaimEntity> findByTenantCodeIgnoreCaseOrderByCreatedAtDesc(String tenantCode);
    List<ExpenseClaimEntity> findByTenantCodeIgnoreCaseAndEmployeeIdOrderByCreatedAtDesc(String tenantCode, String employeeId);
    List<ExpenseClaimEntity> findByTenantCodeIgnoreCaseAndStatusIgnoreCaseOrderByCreatedAtDesc(String tenantCode, String status);
}

