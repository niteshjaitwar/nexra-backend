package com.nexra.hrms.nexra.modules.crm.repository;

import com.nexra.hrms.nexra.modules.crm.entity.IntegrationWebhookSubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IntegrationWebhookSubscriptionRepository extends JpaRepository<IntegrationWebhookSubscriptionEntity, String> {

    List<IntegrationWebhookSubscriptionEntity> findAllByTenantCodeIgnoreCaseAndProductKeyIgnoreCaseOrderByEventTypeAsc(
        String tenantCode,
        String productKey
    );
}
