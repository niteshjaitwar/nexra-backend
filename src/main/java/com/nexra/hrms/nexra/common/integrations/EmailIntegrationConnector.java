package com.nexra.hrms.nexra.common.integrations;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class EmailIntegrationConnector implements IntegrationConnector {

    @Override
    public String connectorType() {
        return "EMAIL";
    }

    @Override
    public Map<String, Object> healthCheck(final Map<String, String> configuration) {
        return Map.of(
            "connector", connectorType(),
            "status", "READY",
            "provider", configuration.getOrDefault("provider", "UNCONFIGURED")
        );
    }
}
