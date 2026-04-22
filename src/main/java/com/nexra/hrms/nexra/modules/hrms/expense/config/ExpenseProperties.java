package com.nexra.hrms.nexra.modules.hrms.expense.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.expense")
public class ExpenseProperties {
    private final Security security = new Security();
    public Security getSecurity() { return security; }
    public static class Security {
        private String jwtSecret = "";
        public String getJwtSecret() { return jwtSecret; }
        public void setJwtSecret(final String jwtSecret) { this.jwtSecret = jwtSecret; }
    }
}

