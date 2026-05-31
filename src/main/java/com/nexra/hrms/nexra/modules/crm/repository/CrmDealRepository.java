package com.nexra.hrms.nexra.modules.crm.repository;

import com.nexra.hrms.nexra.modules.crm.entity.CrmDealEntity;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CrmDealRepository extends JpaRepository<CrmDealEntity, String> {

    Optional<CrmDealEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);

    Page<CrmDealEntity> findAllByTenantCodeIgnoreCaseOrderByUpdatedAtDescIdDesc(String tenantCode, Pageable pageable);
    Page<CrmDealEntity> findAllByTenantCodeIgnoreCaseAndOwnerUserIdOrderByUpdatedAtDescIdDesc(String tenantCode, String ownerUserId, Pageable pageable);

    long countByTenantCodeIgnoreCase(String tenantCode);

    long countByTenantCodeIgnoreCaseAndStageIgnoreCase(String tenantCode, String stage);

    @Query("""
        SELECT COALESCE(SUM(d.valueAmount), 0)
        FROM CrmDealEntity d
        WHERE UPPER(d.tenantCode) = UPPER(:tenantCode)
          AND UPPER(d.stage) NOT IN :closedStages
        """)
    BigDecimal sumOpenPipelineValueByTenantCode(
        @Param("tenantCode") String tenantCode,
        @Param("closedStages") java.util.Collection<String> closedStages
    );

    long countByTenantCodeIgnoreCaseAndStageIn(String tenantCode, java.util.Collection<String> stages);
}
