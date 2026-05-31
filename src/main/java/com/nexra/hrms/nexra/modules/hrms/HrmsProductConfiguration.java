package com.nexra.hrms.nexra.modules.hrms;

import com.nexra.hrms.nexra.modules.hrms.config.HrmsProductSummaryProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(HrmsProductSummaryProperties.class)
public class HrmsProductConfiguration {
}
