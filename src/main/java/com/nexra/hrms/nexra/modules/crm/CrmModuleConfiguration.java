package com.nexra.hrms.nexra.modules.crm;

import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Root configuration boundary for CRM features.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CrmProperties.class)
public class CrmModuleConfiguration {
}
