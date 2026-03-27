package com.nexra.hrms.nexra.modules.hrms.performance.repository; import com.nexra.hrms.nexra.modules.hrms.performance.entity.GoalEntity; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface GoalRepository extends JpaRepository<GoalEntity,String>{ Optional<GoalEntity> findByTenantCodeAndGoalId(String tenantCode,String goalId); List<GoalEntity> findByTenantCodeOrderByCreatedAtDesc(String tenantCode); long countByTenantCode(String tenantCode); long countByTenantCodeAndStatus(String tenantCode,String status); }

