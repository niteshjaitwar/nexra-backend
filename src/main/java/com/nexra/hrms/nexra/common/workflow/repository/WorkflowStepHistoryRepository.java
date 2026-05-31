package com.nexra.hrms.nexra.common.workflow.repository;

import com.nexra.hrms.nexra.common.workflow.entity.WorkflowStepHistoryEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowStepHistoryRepository extends JpaRepository<WorkflowStepHistoryEntity, String> {

    List<WorkflowStepHistoryEntity> findAllByInstanceIdOrderByStepIndexAscCreatedAtAsc(String instanceId);
}
