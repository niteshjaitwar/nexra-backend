package com.nexra.hrms.nexra.modules.crm.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private List<String> closedDealStages = new ArrayList<>(List.of("WON", "CLOSED_LOST"));
    private String wonLeadStatus = "WON";
    private String wonDealStage = "WON";
    private List<String> allowedActivityTypes = new ArrayList<>(List.of("CALL", "EMAIL", "MEETING", "NOTE", "TASK"));
    private final Webhook webhook = new Webhook();
    private final Case caseConfig = new Case();
    private final Campaign campaign = new Campaign();
    private final Quote quote = new Quote();
    private final Deal deal = new Deal();

    static boolean transitionAllowed(final Map<String, List<String>> transitions, final String current, final String target) {
        if (current == null || target == null) {
            return false;
        }
        final String from = current.trim().toUpperCase(Locale.ROOT);
        final String to = target.trim().toUpperCase(Locale.ROOT);
        if (from.equals(to)) {
            return false;
        }
        final List<String> allowed = transitions.get(from);
        if (allowed == null) {
            return false;
        }
        return allowed.stream().anyMatch((status) -> status.trim().toUpperCase(Locale.ROOT).equals(to));
    }

    static boolean knownStatus(final Map<String, List<String>> transitions, final String status) {
        if (status == null) {
            return false;
        }
        final String normalized = status.trim().toUpperCase(Locale.ROOT);
        return transitions.containsKey(normalized)
            || transitions.values().stream()
                .flatMap(List::stream)
                .anyMatch((value) -> value.trim().toUpperCase(Locale.ROOT).equals(normalized));
    }

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

    public List<String> getClosedDealStagesNormalized() {
        return closedDealStages.stream()
            .map((stage) -> stage.trim().toUpperCase(Locale.ROOT))
            .filter((stage) -> !stage.isBlank())
            .distinct()
            .toList();
    }

    public List<String> getClosedDealStages() {
        return closedDealStages;
    }

    public void setClosedDealStages(final List<String> closedDealStages) {
        this.closedDealStages = closedDealStages;
    }

    public String getWonLeadStatusNormalized() {
        return wonLeadStatus == null ? "WON" : wonLeadStatus.trim().toUpperCase(Locale.ROOT);
    }

    public String getWonLeadStatus() {
        return wonLeadStatus;
    }

    public void setWonLeadStatus(final String wonLeadStatus) {
        this.wonLeadStatus = wonLeadStatus;
    }

    public String getWonDealStageNormalized() {
        return wonDealStage == null ? "WON" : wonDealStage.trim().toUpperCase(Locale.ROOT);
    }

    public String getWonDealStage() {
        return wonDealStage;
    }

    public void setWonDealStage(final String wonDealStage) {
        this.wonDealStage = wonDealStage;
    }

    public List<String> getAllowedActivityTypesNormalized() {
        return allowedActivityTypes.stream()
            .map((type) -> type.trim().toUpperCase(Locale.ROOT))
            .filter((type) -> !type.isBlank())
            .distinct()
            .toList();
    }

    public List<String> getAllowedActivityTypes() {
        return allowedActivityTypes;
    }

    public void setAllowedActivityTypes(final List<String> allowedActivityTypes) {
        this.allowedActivityTypes = allowedActivityTypes;
    }

    public Webhook getWebhook() {
        return webhook;
    }

    public Case getCaseConfig() {
        return caseConfig;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public Quote getQuote() {
        return quote;
    }

    public Deal getDeal() {
        return deal;
    }

    /**
     * State-machine configuration for sales deal pipeline stages.
     */
    public static class Deal {

        private String defaultStage = "PROSPECTING";
        private Map<String, List<String>> stageTransitions = defaultTransitions();

        private static Map<String, List<String>> defaultTransitions() {
            final Map<String, List<String>> transitions = new LinkedHashMap<>();
            transitions.put("PROSPECTING", new ArrayList<>(List.of("QUALIFICATION", "CLOSED_LOST")));
            transitions.put("QUALIFICATION", new ArrayList<>(List.of("PROPOSAL", "CLOSED_LOST")));
            transitions.put("PROPOSAL", new ArrayList<>(List.of("NEGOTIATION", "CLOSED_LOST")));
            transitions.put("NEGOTIATION", new ArrayList<>(List.of("WON", "CLOSED_LOST")));
            transitions.put("WON", new ArrayList<>());
            transitions.put("CLOSED_LOST", new ArrayList<>());
            return transitions;
        }

        public String getDefaultStage() {
            return defaultStage == null ? "PROSPECTING" : defaultStage.trim().toUpperCase(Locale.ROOT);
        }

        public void setDefaultStage(final String defaultStage) {
            this.defaultStage = defaultStage;
        }

        public Map<String, List<String>> getStageTransitions() {
            return stageTransitions;
        }

        public void setStageTransitions(final Map<String, List<String>> stageTransitions) {
            this.stageTransitions = stageTransitions;
        }

        public boolean isTransitionAllowed(final String current, final String target) {
            return CrmProperties.transitionAllowed(stageTransitions, current, target);
        }

        public boolean isKnownStage(final String stage) {
            return knownStatus(stageTransitions, stage);
        }
    }

    /**
     * State-machine configuration for marketing campaigns. The lifecycle and the
     * allowed campaign types are configurable per deployment.
     */
    public static class Campaign {

        private String defaultStatus = "DRAFT";
        private List<String> allowedTypes = new ArrayList<>(List.of(
            "EMAIL", "SOCIAL", "EVENT", "WEBINAR", "PAID_ADS", "CONTENT"));
        private Map<String, List<String>> statusTransitions = defaultTransitions();

        private static Map<String, List<String>> defaultTransitions() {
            final Map<String, List<String>> transitions = new LinkedHashMap<>();
            transitions.put("DRAFT", new ArrayList<>(List.of("ACTIVE", "CANCELLED")));
            transitions.put("ACTIVE", new ArrayList<>(List.of("PAUSED", "COMPLETED", "CANCELLED")));
            transitions.put("PAUSED", new ArrayList<>(List.of("ACTIVE", "COMPLETED", "CANCELLED")));
            transitions.put("COMPLETED", new ArrayList<>());
            transitions.put("CANCELLED", new ArrayList<>());
            return transitions;
        }

        public String getDefaultStatus() {
            return defaultStatus == null ? "DRAFT" : defaultStatus.trim().toUpperCase(Locale.ROOT);
        }

        public void setDefaultStatus(final String defaultStatus) {
            this.defaultStatus = defaultStatus;
        }

        public List<String> getAllowedTypes() {
            return allowedTypes;
        }

        public List<String> getAllowedTypesNormalized() {
            return allowedTypes.stream()
                .map((type) -> type.trim().toUpperCase(Locale.ROOT))
                .filter((type) -> !type.isBlank())
                .distinct()
                .toList();
        }

        public void setAllowedTypes(final List<String> allowedTypes) {
            this.allowedTypes = allowedTypes;
        }

        public Map<String, List<String>> getStatusTransitions() {
            return statusTransitions;
        }

        public void setStatusTransitions(final Map<String, List<String>> statusTransitions) {
            this.statusTransitions = statusTransitions;
        }

        public boolean isTransitionAllowed(final String current, final String target) {
            return transitionAllowed(statusTransitions, current, target);
        }

        public boolean isKnownStatus(final String status) {
            return knownStatus(statusTransitions, status);
        }
    }

    /**
     * State-machine and financial-default configuration for sales quotes.
     */
    public static class Quote {

        private String defaultStatus = "DRAFT";
        private String defaultCurrency = "INR";
        private String acceptedDealStage;
        private boolean autoCreateOpsProjectOnAccept = false;
        private Map<String, List<String>> statusTransitions = defaultTransitions();

        private static Map<String, List<String>> defaultTransitions() {
            final Map<String, List<String>> transitions = new LinkedHashMap<>();
            transitions.put("DRAFT", new ArrayList<>(List.of("SENT", "CANCELLED")));
            transitions.put("SENT", new ArrayList<>(List.of("ACCEPTED", "REJECTED", "EXPIRED", "CANCELLED")));
            transitions.put("ACCEPTED", new ArrayList<>());
            transitions.put("REJECTED", new ArrayList<>());
            transitions.put("EXPIRED", new ArrayList<>());
            transitions.put("CANCELLED", new ArrayList<>());
            return transitions;
        }

        public String getDefaultStatus() {
            return defaultStatus == null ? "DRAFT" : defaultStatus.trim().toUpperCase(Locale.ROOT);
        }

        public void setDefaultStatus(final String defaultStatus) {
            this.defaultStatus = defaultStatus;
        }

        public String getDefaultCurrency() {
            return defaultCurrency == null ? "INR" : defaultCurrency.trim().toUpperCase(Locale.ROOT);
        }

        public void setDefaultCurrency(final String defaultCurrency) {
            this.defaultCurrency = defaultCurrency;
        }

        public String getAcceptedDealStage() {
            return acceptedDealStage;
        }

        public void setAcceptedDealStage(final String acceptedDealStage) {
            this.acceptedDealStage = acceptedDealStage;
        }

        public boolean isAutoCreateOpsProjectOnAccept() {
            return autoCreateOpsProjectOnAccept;
        }

        public void setAutoCreateOpsProjectOnAccept(final boolean autoCreateOpsProjectOnAccept) {
            this.autoCreateOpsProjectOnAccept = autoCreateOpsProjectOnAccept;
        }

        /**
         * Resolves the deal stage applied when a quote is accepted. Falls back to the
         * tenant-wide won deal stage when not explicitly configured.
         */
        public String resolveAcceptedDealStage(final String wonDealStageFallback) {
            if (acceptedDealStage == null || acceptedDealStage.isBlank()) {
                return wonDealStageFallback;
            }
            return acceptedDealStage.trim().toUpperCase(Locale.ROOT);
        }

        public Map<String, List<String>> getStatusTransitions() {
            return statusTransitions;
        }

        public void setStatusTransitions(final Map<String, List<String>> statusTransitions) {
            this.statusTransitions = statusTransitions;
        }

        public boolean isTransitionAllowed(final String current, final String target) {
            return transitionAllowed(statusTransitions, current, target);
        }

        public boolean isKnownStatus(final String status) {
            return knownStatus(statusTransitions, status);
        }
    }

    /**
     * Externalized state-machine configuration for CRM support cases. Statuses and
     * the allowed transitions between them are configurable so tenants/regions can
     * adapt the lifecycle without code changes.
     */
    public static class Case {

        private String defaultStatus = "OPEN";
        private String defaultPriority = "MEDIUM";
        private List<String> closedStatuses = new ArrayList<>(List.of("CLOSED", "CANCELLED"));
        private Map<String, List<String>> statusTransitions = defaultTransitions();

        private static Map<String, List<String>> defaultTransitions() {
            final Map<String, List<String>> transitions = new LinkedHashMap<>();
            transitions.put("OPEN", new ArrayList<>(List.of("IN_PROGRESS", "ON_HOLD", "RESOLVED", "CANCELLED")));
            transitions.put("IN_PROGRESS", new ArrayList<>(List.of("ON_HOLD", "RESOLVED", "CANCELLED")));
            transitions.put("ON_HOLD", new ArrayList<>(List.of("IN_PROGRESS", "RESOLVED", "CANCELLED")));
            transitions.put("RESOLVED", new ArrayList<>(List.of("CLOSED", "IN_PROGRESS")));
            transitions.put("CLOSED", new ArrayList<>(List.of("IN_PROGRESS")));
            return transitions;
        }

        public String getDefaultStatus() {
            return defaultStatus == null ? "OPEN" : defaultStatus.trim().toUpperCase(Locale.ROOT);
        }

        public void setDefaultStatus(final String defaultStatus) {
            this.defaultStatus = defaultStatus;
        }

        public String getDefaultPriority() {
            return defaultPriority == null ? "MEDIUM" : defaultPriority.trim().toUpperCase(Locale.ROOT);
        }

        public void setDefaultPriority(final String defaultPriority) {
            this.defaultPriority = defaultPriority;
        }

        public List<String> getClosedStatuses() {
            return closedStatuses;
        }

        public List<String> getClosedStatusesNormalized() {
            return closedStatuses.stream()
                .map((status) -> status.trim().toUpperCase(Locale.ROOT))
                .filter((status) -> !status.isBlank())
                .distinct()
                .toList();
        }

        public void setClosedStatuses(final List<String> closedStatuses) {
            this.closedStatuses = closedStatuses;
        }

        public Map<String, List<String>> getStatusTransitions() {
            return statusTransitions;
        }

        public void setStatusTransitions(final Map<String, List<String>> statusTransitions) {
            this.statusTransitions = statusTransitions;
        }

        /**
         * Returns true when moving from {@code current} to {@code target} is a
         * permitted transition. A no-op transition to the same status is rejected
         * so callers must always change state.
         *
         * @param current the existing case status.
         * @param target  the requested next status.
         * @return whether the transition is allowed by configuration.
         */
        public boolean isTransitionAllowed(final String current, final String target) {
            if (current == null || target == null) {
                return false;
            }
            final String from = current.trim().toUpperCase(Locale.ROOT);
            final String to = target.trim().toUpperCase(Locale.ROOT);
            if (from.equals(to)) {
                return false;
            }
            final List<String> allowed = statusTransitions.get(from);
            if (allowed == null) {
                return false;
            }
            return allowed.stream().anyMatch((status) -> status.trim().toUpperCase(Locale.ROOT).equals(to));
        }

        public boolean isKnownStatus(final String status) {
            if (status == null) {
                return false;
            }
            final String normalized = status.trim().toUpperCase(Locale.ROOT);
            return statusTransitions.containsKey(normalized)
                || statusTransitions.values().stream()
                    .flatMap(List::stream)
                    .anyMatch((value) -> value.trim().toUpperCase(Locale.ROOT).equals(normalized));
        }
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
