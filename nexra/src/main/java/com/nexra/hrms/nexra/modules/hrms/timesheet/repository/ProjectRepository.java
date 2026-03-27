package com.nexra.hrms.nexra.modules.hrms.timesheet.repository;

import com.nexra.hrms.nexra.modules.hrms.timesheet.entity.ProjectEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<ProjectEntity, String> {
    Optional<ProjectEntity> findByTenantCodeIgnoreCaseAndProjectCodeIgnoreCase(String tenantCode, String projectCode);
    List<ProjectEntity> findByTenantCodeIgnoreCaseOrderByProjectCodeAsc(String tenantCode);
}

