package com.nexra.hrms.nexra.modules.hrms.expense.repository;

import com.nexra.hrms.nexra.modules.hrms.expense.entity.ExpenseClaimItemEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseClaimItemRepository extends JpaRepository<ExpenseClaimItemEntity, String> {
    List<ExpenseClaimItemEntity> findByClaimIdOrderByExpenseDateAsc(String claimId);
}

