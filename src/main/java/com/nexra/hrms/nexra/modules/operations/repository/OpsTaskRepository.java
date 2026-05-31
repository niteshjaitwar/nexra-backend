package com.nexra.hrms.nexra.modules.operations.repository;

import com.nexra.hrms.nexra.modules.operations.entity.OpsTaskEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpsTaskRepository extends JpaRepository<OpsTaskEntity, String> {

    Optional<OpsTaskEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);

    Page<OpsTaskEntity> findAllByTenantCodeIgnoreCaseAndProjectIdOrderByUpdatedAtDescIdDesc(
        String tenantCode,
        String projectId,
        Pageable pageable
    );
}
