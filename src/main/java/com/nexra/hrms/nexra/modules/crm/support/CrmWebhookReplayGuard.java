package com.nexra.hrms.nexra.modules.crm.support;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class CrmWebhookReplayGuard {

    private final CrmProperties crmProperties;
    private volatile Cache<String, Boolean> replayCache;

    public boolean markIfFirstSeen(
        final String tenantCode,
        final String idempotencyKey,
        final String timestamp,
        final String signature
    ) {
        final String key = (tenantCode + "|" + idempotencyKey + "|" + timestamp + "|" + signature).toUpperCase(Locale.ROOT);
        if (cache().asMap().putIfAbsent(key, Boolean.TRUE) != null) {
            return false;
        }
        return true;
    }

    private Cache<String, Boolean> cache() {
        Cache<String, Boolean> local = replayCache;
        if (local != null) {
            return local;
        }
        synchronized (this) {
            if (replayCache == null) {
                final long ttlSeconds = Math.max(60, crmProperties.getWebhook().getReplayCacheTtlSeconds());
                final long maxEntries = Math.max(10_000, crmProperties.getWebhook().getReplayCacheMaxEntries());
                replayCache = Caffeine.newBuilder()
                    .expireAfterWrite(Duration.ofSeconds(ttlSeconds))
                    .maximumSize(maxEntries)
                    .build();
            }
            return replayCache;
        }
    }
}
