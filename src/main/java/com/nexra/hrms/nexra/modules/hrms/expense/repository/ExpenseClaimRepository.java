package com.nexra.hrms.nexra.modules.hrms.expense.repository;

import com.nexra.hrms.nexra.modules.hrms.expense.entity.ExpenseClaimEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseClaimRepository extends JpaRepository<ExpenseClaimEntity, String> {
    Optional<ExpenseClaimEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);
    List<ExpenseClaimEntity> findByTenantCodeIgnoreCaseOrderByCreatedAtDesc(String tenantCode);
    List<ExpenseClaimEntity> findByTenantCodeIgnoreCaseAndEmployeeIdOrderByCreatedAtDesc(String tenantCode, String employeeId);
    List<ExpenseClaimEntity> findByTenantCodeIgnoreCaseAndStatusIgnoreCaseOrderByCreatedAtDesc(String tenantCode, String status);

    // Paginated queries
    Page<ExpenseClaimEntity> findByTenantCodeIgnoreCase(String tenantCode, Pageable pageable);
    Page<ExpenseClaimEntity> findByTenantCodeIgnoreCaseAndEmployeeId(String tenantCode, String employeeId, Pageable pageable);
    Page<ExpenseClaimEntity> findByTenantCodeIgnoreCaseAndStatusIgnoreCase(String tenantCode, String status, Pageable pageable);
    Page<ExpenseClaimEntity> findByTenantCodeIgnoreCaseAndEmployeeIdAndStatusIgnoreCase(String tenantCode, String employeeId, String status, Pageable pageable);

    long countByTenantCodeIgnoreCase(String tenantCode);

    long countByTenantCodeIgnoreCaseAndStatusIgnoreCase(String tenantCode, String status);
}
