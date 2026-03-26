package com.nexra.hrms.nexra.modules.payroll.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Fails startup when payroll production configuration is unsafe.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class PayrollProductionReadinessValidator implements ApplicationRunner {

    private final Environment environment;
    private final PayrollProperties payrollProperties;

    @Override
    public void run(final ApplicationArguments args) {
        if (isNonProductionProfileActive()) {
            return;
        }

        String jwtSecret = payrollProperties.getSecurity().getJwtSecret();
        assertCondition(jwtSecret != null && !jwtSecret.isBlank(), "AUTH_JWT_SECRET must be configured.");
        assertCondition(jwtSecret.length() >= 32, "AUTH_JWT_SECRET must be at least 32 characters.");
        assertCondition(hasText(payrollProperties.getBrand().getCompanyName()), "Payroll brand company name must be configured.");
        assertCondition(hasText(payrollProperties.getBrand().getBannerPath()), "Payroll brand banner path must be configured.");
        assertCondition(payrollProperties.getBrand().getBannerPath().startsWith("/"), "Payroll brand banner path must start with '/'.");
        assertCondition(hasText(payrollProperties.getBrand().getWatermarkText()), "Payroll brand watermark text must be configured.");
    }

    private boolean isNonProductionProfileActive() {
        for (String activeProfile : environment.getActiveProfiles()) {
            if ("dev".equalsIgnoreCase(activeProfile)
                || "test".equalsIgnoreCase(activeProfile)
                || "e2e".equalsIgnoreCase(activeProfile)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasText(final String value) {
        return value != null && !value.isBlank();
    }

    private void assertCondition(final boolean condition, final String message) {
        if (!condition) {
            throw new IllegalStateException("Production configuration invalid: " + message);
        }
    }
}
