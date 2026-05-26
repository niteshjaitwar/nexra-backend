package com.nexra.hrms.nexra.modules.hrms.onboarding.repository;

import com.nexra.hrms.nexra.modules.hrms.onboarding.entity.OnboardingTaskEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardingTaskRepository extends JpaRepository<OnboardingTaskEntity, String> {

    Optional<OnboardingTaskEntity> findByTenantCodeAndTaskId(String tenantCode, String taskId);

    List<OnboardingTaskEntity> findByTenantCodeAndPlanIdOrderByCreatedAtAsc(String tenantCode, String planId);

    long countByTenantCode(String tenantCode);

    long countByTenantCodeAndStatus(String tenantCode, String status);
}
