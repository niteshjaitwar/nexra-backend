package com.nexra.hrms.nexra.modules.hrms.performance.repository; import com.nexra.hrms.nexra.modules.hrms.performance.entity.ReviewEntity; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface ReviewRepository extends JpaRepository<ReviewEntity,String>{ Optional<ReviewEntity> findByTenantCodeAndReviewId(String tenantCode,String reviewId); List<ReviewEntity> findByTenantCodeOrderByCreatedAtDesc(String tenantCode); long countByTenantCode(String tenantCode); long countByTenantCodeAndStatus(String tenantCode,String status); }

