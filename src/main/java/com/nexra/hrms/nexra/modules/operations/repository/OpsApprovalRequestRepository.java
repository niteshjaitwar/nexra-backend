package com.nexra.hrms.nexra.modules.operations.repository;

import com.nexra.hrms.nexra.modules.operations.entity.OpsApprovalRequestEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpsApprovalRequestRepository extends JpaRepository<OpsApprovalRequestEntity, String> {

    Optional<OpsApprovalRequestEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);

    Page<OpsApprovalRequestEntity> findAllByTenantCodeIgnoreCaseOrderByUpdatedAtDescIdDesc(
        String tenantCode,
        Pageable pageable
    );
}
