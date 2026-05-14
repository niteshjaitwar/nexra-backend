package com.nexra.hrms.nexra.modules.hrms.performance.repository;

import com.nexra.hrms.nexra.modules.hrms.performance.entity.ReviewEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<ReviewEntity, String> {
    Optional<ReviewEntity> findByTenantCodeAndReviewId(String tenantCode, String reviewId);
    List<ReviewEntity> findByTenantCodeOrderByCreatedAtDesc(String tenantCode);
    long countByTenantCode(String tenantCode);
    long countByTenantCodeAndStatus(String tenantCode, String status);

    // Paginated queries
    Page<ReviewEntity> findByTenantCode(String tenantCode, Pageable pageable);
    Page<ReviewEntity> findByTenantCodeAndEmployeeId(String tenantCode, String employeeId, Pageable pageable);
    Page<ReviewEntity> findByTenantCodeAndStatusIgnoreCase(String tenantCode, String status, Pageable pageable);
    Page<ReviewEntity> findByTenantCodeAndEmployeeIdAndStatusIgnoreCase(String tenantCode, String employeeId, String status, Pageable pageable);
}
