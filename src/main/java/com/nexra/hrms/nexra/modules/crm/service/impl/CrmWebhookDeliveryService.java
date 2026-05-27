package com.nexra.hrms.nexra.modules.crm.service.impl;

import com.nexra.hrms.nexra.modules.crm.entity.IntegrationWebhookDeliveryEntity;
import com.nexra.hrms.nexra.modules.crm.entity.IntegrationWebhookDeliveryStatus;
import com.nexra.hrms.nexra.modules.crm.entity.IntegrationWebhookSubscriptionEntity;
import com.nexra.hrms.nexra.modules.crm.repository.IntegrationWebhookDeliveryRepository;
import com.nexra.hrms.nexra.modules.crm.repository.IntegrationWebhookSubscriptionRepository;
import com.nexra.hrms.nexra.modules.crm.support.CrmWebhookSignatureCodec;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrmWebhookDeliveryService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long INITIAL_BACKOFF_SECONDS = 30;
    private final IntegrationWebhookDeliveryRepository deliveryRepository;
    private final IntegrationWebhookSubscriptionRepository subscriptionRepository;
    private final MeterRegistry meterRegistry;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(5)).build();

    @PostConstruct
    void registerWebhookDeliveryGauges() {
        Gauge.builder("nexra.crm.webhook.delivery.count", deliveryRepository,
                repository -> repository.countByStatus(IntegrationWebhookDeliveryStatus.PENDING))
            .description("Webhook delivery records currently in PENDING status.")
            .tag("status", "pending")
            .register(meterRegistry);
        Gauge.builder("nexra.crm.webhook.delivery.count", deliveryRepository,
                repository -> repository.countByStatus(IntegrationWebhookDeliveryStatus.RETRYING))
            .description("Webhook delivery records currently in RETRYING status.")
            .tag("status", "retrying")
            .register(meterRegistry);
        Gauge.builder("nexra.crm.webhook.delivery.count", deliveryRepository,
                repository -> repository.countByStatus(IntegrationWebhookDeliveryStatus.DEAD_LETTER))
            .description("Webhook delivery records currently in DEAD_LETTER status.")
            .tag("status", "dead_letter")
            .register(meterRegistry);
    }

    @Transactional
    public void enqueueForSubscription(
        final IntegrationWebhookSubscriptionEntity subscription,
        final String payloadJson
    ) {
        final String payload = payloadJson == null ? "{}" : payloadJson.trim();
        final IntegrationWebhookDeliveryEntity delivery = new IntegrationWebhookDeliveryEntity();
        delivery.setId(UUID.randomUUID().toString());
        delivery.setTenantCode(subscription.getTenantCode());
        delivery.setSubscription(subscription);
        delivery.setProductKey(subscription.getProductKey());
        delivery.setEventType(subscription.getEventType());
        delivery.setPayloadJson(payload);
        delivery.setPayloadHash(sha256Hex(payload));
        delivery.setIdempotencyKey(UUID.randomUUID().toString());
        delivery.setStatus(IntegrationWebhookDeliveryStatus.PENDING);
        delivery.setAttemptCount(0);
        delivery.setMaxAttempts(MAX_ATTEMPTS);
        delivery.setNextAttemptAt(Instant.now());
        deliveryRepository.save(delivery);
        counter("enqueued", subscription.getTenantCode()).increment();
    }

    @Scheduled(fixedDelayString = "${nexra.crm.webhooks.dispatch-fixed-delay-ms:5000}")
    @Transactional
    public void dispatchDueDeliveries() {
        final List<IntegrationWebhookDeliveryEntity> dueDeliveries =
            deliveryRepository.findTop50ByStatusInAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(
                EnumSet.of(IntegrationWebhookDeliveryStatus.PENDING, IntegrationWebhookDeliveryStatus.RETRYING),
                Instant.now()
            );
        for (IntegrationWebhookDeliveryEntity delivery : dueDeliveries) {
            dispatchSingle(delivery);
        }
    }

    private void dispatchSingle(final IntegrationWebhookDeliveryEntity delivery) {
        final IntegrationWebhookSubscriptionEntity subscription = delivery.getSubscription();
        if (!subscription.isActive()) {
            delivery.setStatus(IntegrationWebhookDeliveryStatus.DEAD_LETTER);
            delivery.setLastError("Subscription is inactive.");
            counter("dead_lettered", subscription.getTenantCode()).increment();
            return;
        }
        try {
            final Instant now = Instant.now();
            final String timestamp = String.valueOf(now.getEpochSecond());
            final String signature = CrmWebhookSignatureCodec.buildSignature(
                subscription.getSecretHash(),
                delivery.getPayloadJson(),
                delivery.getIdempotencyKey(),
                timestamp
            );
            final HttpRequest request = HttpRequest.newBuilder(URI.create(subscription.getTargetUrl()))
                .header("Content-Type", "application/json")
                .header("X-Nexra-Idempotency-Key", delivery.getIdempotencyKey())
                .header("X-Nexra-Event-Type", delivery.getEventType())
                .header("X-Nexra-Timestamp", timestamp)
                .header("X-Nexra-Signature-Algorithm", CrmWebhookSignatureCodec.SIGNATURE_ALGORITHM)
                .header("X-Nexra-Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(delivery.getPayloadJson()))
                .build();
            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            final int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 300) {
                delivery.setStatus(IntegrationWebhookDeliveryStatus.SUCCEEDED);
                delivery.setDeliveredAt(Instant.now());
                delivery.setLastStatusCode(statusCode);
                delivery.setLastError(null);
                subscription.setFailureCount(0);
                subscription.setLastSuccessAt(Instant.now());
                subscriptionRepository.save(subscription);
                counter("succeeded", subscription.getTenantCode()).increment();
                return;
            }
            markFailure(delivery, subscription, statusCode, "Non-success webhook response");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            markFailure(delivery, subscription, null, truncate(ex.getMessage()));
        } catch (IOException ex) {
            markFailure(delivery, subscription, null, truncate(ex.getMessage()));
        } catch (RuntimeException ex) {
            markFailure(delivery, subscription, null, truncate(ex.getMessage()));
        }
    }

    private void markFailure(
        final IntegrationWebhookDeliveryEntity delivery,
        final IntegrationWebhookSubscriptionEntity subscription,
        final Integer statusCode,
        final String reason
    ) {
        final int attempts = delivery.getAttemptCount() + 1;
        delivery.setAttemptCount(attempts);
        delivery.setLastFailureAt(Instant.now());
        delivery.setLastStatusCode(statusCode);
        delivery.setLastError(reason);
        subscription.setFailureCount(subscription.getFailureCount() + 1);
        subscription.setLastFailureAt(Instant.now());
        subscriptionRepository.save(subscription);
        if (attempts >= delivery.getMaxAttempts()) {
            delivery.setStatus(IntegrationWebhookDeliveryStatus.DEAD_LETTER);
            counter("dead_lettered", subscription.getTenantCode()).increment();
            return;
        }
        final long waitSeconds = INITIAL_BACKOFF_SECONDS * (1L << Math.min(4, attempts - 1));
        delivery.setStatus(IntegrationWebhookDeliveryStatus.RETRYING);
        delivery.setNextAttemptAt(Instant.now().plus(waitSeconds, ChronoUnit.SECONDS));
        counter("retry_scheduled", subscription.getTenantCode()).increment();
    }

    private String truncate(final String value) {
        if (value == null) {
            return "Unknown webhook delivery error.";
        }
        return value.length() <= 500 ? value : value.substring(0, 500);
    }

    private String sha256Hex(final String value) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 digest is unavailable.", ex);
        }
    }

    private Counter counter(final String event, final String tenantCode) {
        return Counter.builder("nexra.crm.webhook.delivery.events")
            .description("Webhook delivery lifecycle events.")
            .tag("event", event)
            .tag("tenant", tenantCode == null ? "UNKNOWN" : tenantCode.toUpperCase())
            .register(meterRegistry);
    }
}
