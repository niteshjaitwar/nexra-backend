package com.nexra.hrms.nexra.modules.payroll.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.payroll")
public class PayrollProperties {

    @Valid
    private final Brand brand = new Brand();

    @Valid
    private final TenantBranding tenantBranding = new TenantBranding();

    @Valid
    private final Security security = new Security();

    private final Statutory statutory = new Statutory();

    @Getter
    @Setter
    public static class Brand {
        @NotBlank
        private String companyName;
        @NotBlank
        private String bannerPath;
        @NotBlank
        private String watermarkText;
    }

    @Getter
    @Setter
    public static class Security {
        @NotBlank
        private String jwtSecret;
    }

    @Getter
    @Setter
    public static class TenantBranding {
        @NotBlank
        private String logoStoragePath;
        @NotBlank
        private String publicLogoBasePath;
        private long maxLogoBytes = 2_097_152L;
    }

    /**
     * Statutory filing configuration. Maps an ISO country code to the filing
     * artifact type generated for that jurisdiction.
     */
    @Getter
    @Setter
    public static class Statutory {
        private Map<String, String> filingTypes = new HashMap<>(Map.of(
            "IN", "PF_ESI_PT_RETURN",
            "US", "FORM_941",
            "GB", "RTI_FPS",
            "DE", "LOHNSTEUERANMELDUNG",
            "AE", "WPS_RETURN",
            "SG", "CPF_FPS"
        ));
        private String defaultFilingType = "STATUTORY_RETURN";

        public String resolveFilingType(final String countryCode) {
            if (countryCode == null) {
                return defaultFilingType;
            }
            return filingTypes.getOrDefault(countryCode.trim().toUpperCase(Locale.ROOT), defaultFilingType);
        }
    }
}
