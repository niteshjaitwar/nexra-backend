package com.nexra.hrms.nexra.modules.operations;

import com.nexra.hrms.nexra.modules.operations.config.OperationsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OperationsProperties.class)
public class OperationsModuleConfiguration {
}
