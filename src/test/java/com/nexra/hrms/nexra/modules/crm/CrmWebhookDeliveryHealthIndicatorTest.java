package com.nexra.hrms.nexra.modules.crm;

import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.entity.IntegrationWebhookDeliveryStatus;
import com.nexra.hrms.nexra.modules.crm.ops.CrmWebhookDeliveryHealthIndicator;
import com.nexra.hrms.nexra.modules.crm.repository.IntegrationWebhookDeliveryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CrmWebhookDeliveryHealthIndicatorTest {

    @Test
    void returnsDownWhenDeadLetterThresholdBreached() {
        final IntegrationWebhookDeliveryRepository repository = mock(IntegrationWebhookDeliveryRepository.class);
        when(repository.countByStatus(IntegrationWebhookDeliveryStatus.RETRYING)).thenReturn(1L);
        when(repository.countByStatus(IntegrationWebhookDeliveryStatus.DEAD_LETTER)).thenReturn(5L);

        final CrmProperties props = new CrmProperties();
        props.getWebhook().setRetryingAlertThreshold(10);
        props.getWebhook().setDeadLetterAlertThreshold(5);

        final CrmWebhookDeliveryHealthIndicator indicator = new CrmWebhookDeliveryHealthIndicator(repository, props);
        assertEquals(Status.DOWN, indicator.health().getStatus());
    }

    @Test
    void returnsOutOfServiceWhenRetryingThresholdBreached() {
        final IntegrationWebhookDeliveryRepository repository = mock(IntegrationWebhookDeliveryRepository.class);
        when(repository.countByStatus(IntegrationWebhookDeliveryStatus.RETRYING)).thenReturn(10L);
        when(repository.countByStatus(IntegrationWebhookDeliveryStatus.DEAD_LETTER)).thenReturn(1L);

        final CrmProperties props = new CrmProperties();
        props.getWebhook().setRetryingAlertThreshold(10);
        props.getWebhook().setDeadLetterAlertThreshold(5);

        final CrmWebhookDeliveryHealthIndicator indicator = new CrmWebhookDeliveryHealthIndicator(repository, props);
        assertEquals(Status.OUT_OF_SERVICE, indicator.health().getStatus());
    }

    @Test
    void returnsUpWhenThresholdsHealthy() {
        final IntegrationWebhookDeliveryRepository repository = mock(IntegrationWebhookDeliveryRepository.class);
        when(repository.countByStatus(IntegrationWebhookDeliveryStatus.RETRYING)).thenReturn(2L);
        when(repository.countByStatus(IntegrationWebhookDeliveryStatus.DEAD_LETTER)).thenReturn(1L);

        final CrmProperties props = new CrmProperties();
        props.getWebhook().setRetryingAlertThreshold(10);
        props.getWebhook().setDeadLetterAlertThreshold(5);

        final CrmWebhookDeliveryHealthIndicator indicator = new CrmWebhookDeliveryHealthIndicator(repository, props);
        assertEquals(Status.UP, indicator.health().getStatus());
    }
}
