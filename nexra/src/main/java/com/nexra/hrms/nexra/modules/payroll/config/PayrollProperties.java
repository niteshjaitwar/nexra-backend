package com.nexra.hrms.nexra.modules.payroll.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
    private final Security security = new Security();

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
}
