package com.nexra.hrms.nexra.modules.hrms.attendance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds attendance module configuration properties.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@ConfigurationProperties(prefix = "app.attendance")
public class AttendanceProperties {

    private final Security security = new Security();

    public Security getSecurity() {
        return security;
    }

    /**
     * Security-related attendance properties.
     */
    public static class Security {

        private String jwtSecret = "";

        public String getJwtSecret() {
            return jwtSecret;
        }

        public void setJwtSecret(final String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }
    }
}

