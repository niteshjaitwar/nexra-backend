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
}
