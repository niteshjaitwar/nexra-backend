package com.nexra.hrms.nexra.common.integrations;

import java.util.Map;

public interface IntegrationConnector {

    String connectorType();

    Map<String, Object> healthCheck(Map<String, String> configuration);
}
