package com.nexra.hrms.nexra.modules.crm.ops;

import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.entity.IntegrationWebhookDeliveryStatus;
import com.nexra.hrms.nexra.modules.crm.repository.IntegrationWebhookDeliveryRepository;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;
import org.springframework.stereotype.Component;

/**
 * Health indicator for CRM webhook delivery pressure.
 * DOWN: dead-letter threshold breached.
 * OUT_OF_SERVICE: retrying threshold breached.
 * UP: thresholds healthy.
 *
 * @author niteshjaitwar
 */
@Component("crmWebhookDelivery")
public class CrmWebhookDeliveryHealthIndicator implements HealthIndicator {

    private final IntegrationWebhookDeliveryRepository deliveryRepository;
    private final CrmProperties crmProperties;

    public CrmWebhookDeliveryHealthIndicator(
        final IntegrationWebhookDeliveryRepository deliveryRepository,
        final CrmProperties crmProperties
    ) {
        this.deliveryRepository = deliveryRepository;
        this.crmProperties = crmProperties;
    }

    @Override
    public Health health() {
        final long retrying = deliveryRepository.countByStatus(IntegrationWebhookDeliveryStatus.RETRYING);
        final long deadLetter = deliveryRepository.countByStatus(IntegrationWebhookDeliveryStatus.DEAD_LETTER);
        final int retryingThreshold = crmProperties.getWebhook().getRetryingAlertThreshold();
        final int deadLetterThreshold = crmProperties.getWebhook().getDeadLetterAlertThreshold();
        final Status status = deadLetter >= deadLetterThreshold
            ? Status.DOWN
            : (retrying >= retryingThreshold ? Status.OUT_OF_SERVICE : Status.UP);
        return Health.status(status)
            .withDetail("retryingCount", retrying)
            .withDetail("retryingThreshold", retryingThreshold)
            .withDetail("deadLetterCount", deadLetter)
            .withDetail("deadLetterThreshold", deadLetterThreshold)
            .build();
    }
}
