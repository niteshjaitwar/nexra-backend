package com.nexra.hrms.nexra.modules.crm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized configuration for CRM module behavior.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@ConfigurationProperties(prefix = "nexra.crm")
public class CrmProperties {

    private boolean enabled = true;
    private int maxPageSize = 100;
    private boolean enforceAuth = true;
    private final Webhook webhook = new Webhook();

    /**
     * Returns whether CRM APIs are enabled.
     *
     * @return true when CRM endpoints are enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether CRM APIs are enabled.
     *
     * @param enabled module enablement flag.
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the maximum allowed page size for CRM list endpoints.
     *
     * @return max page size.
     */
    public int getMaxPageSize() {
        return maxPageSize;
    }

    /**
     * Sets the maximum allowed page size for CRM list endpoints.
     *
     * @param maxPageSize max page size.
     */
    public void setMaxPageSize(final int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    /**
     * Returns whether authentication is required for CRM endpoints.
     *
     * @return true when auth is enforced.
     */
    public boolean isEnforceAuth() {
        return enforceAuth;
    }

    /**
     * Sets whether authentication is required for CRM endpoints.
     *
     * @param enforceAuth auth enforcement flag.
     */
    public void setEnforceAuth(final boolean enforceAuth) {
        this.enforceAuth = enforceAuth;
    }

    public Webhook getWebhook() {
        return webhook;
    }

    public static class Webhook {

        private int deadLetterAlertThreshold = 10;
        private int retryingAlertThreshold = 50;
        private long signatureTimestampSkewSeconds = 300;
        private long replayCacheTtlSeconds = 900;
        private long replayCacheMaxEntries = 100_000;

        public int getDeadLetterAlertThreshold() {
            return deadLetterAlertThreshold;
        }

        public void setDeadLetterAlertThreshold(final int deadLetterAlertThreshold) {
            this.deadLetterAlertThreshold = deadLetterAlertThreshold;
        }

        public int getRetryingAlertThreshold() {
            return retryingAlertThreshold;
        }

        public void setRetryingAlertThreshold(final int retryingAlertThreshold) {
            this.retryingAlertThreshold = retryingAlertThreshold;
        }

        public long getSignatureTimestampSkewSeconds() {
            return signatureTimestampSkewSeconds;
        }

        public void setSignatureTimestampSkewSeconds(final long signatureTimestampSkewSeconds) {
            this.signatureTimestampSkewSeconds = signatureTimestampSkewSeconds;
        }

        public long getReplayCacheTtlSeconds() {
            return replayCacheTtlSeconds;
        }

        public void setReplayCacheTtlSeconds(final long replayCacheTtlSeconds) {
            this.replayCacheTtlSeconds = replayCacheTtlSeconds;
        }

        public long getReplayCacheMaxEntries() {
            return replayCacheMaxEntries;
        }

        public void setReplayCacheMaxEntries(final long replayCacheMaxEntries) {
            this.replayCacheMaxEntries = replayCacheMaxEntries;
        }
    }
}
