package com.nexra.hrms.nexra.modules.crm.repository;

import com.nexra.hrms.nexra.modules.crm.entity.IntegrationWebhookDeliveryEntity;
import com.nexra.hrms.nexra.modules.crm.entity.IntegrationWebhookDeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IntegrationWebhookDeliveryRepository extends JpaRepository<IntegrationWebhookDeliveryEntity, String> {

    List<IntegrationWebhookDeliveryEntity> findTop50ByStatusInAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(
        Collection<IntegrationWebhookDeliveryStatus> statuses,
        Instant dueAt
    );

    long countByTenantCodeIgnoreCaseAndProductKeyIgnoreCase(String tenantCode, String productKey);

    List<IntegrationWebhookDeliveryEntity> findTop100ByTenantCodeIgnoreCaseOrderByCreatedAtDesc(String tenantCode);

    Optional<IntegrationWebhookDeliveryEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);

    long countByTenantCodeIgnoreCaseAndStatus(String tenantCode, IntegrationWebhookDeliveryStatus status);

    long countByStatus(IntegrationWebhookDeliveryStatus status);
}
