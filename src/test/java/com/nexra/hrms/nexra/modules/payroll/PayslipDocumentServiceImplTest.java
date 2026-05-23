package com.nexra.hrms.nexra.modules.payroll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexra.hrms.nexra.modules.payroll.config.PayrollProperties;
import com.nexra.hrms.nexra.modules.payroll.model.AuthDependencyStatus;
import com.nexra.hrms.nexra.modules.payroll.model.EmployeeProfile;
import com.nexra.hrms.nexra.modules.payroll.model.OrganizationProfile;
import com.nexra.hrms.nexra.modules.payroll.model.PayrollSlip;
import com.nexra.hrms.nexra.modules.payroll.service.impl.PayslipDocumentServiceImpl;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@DisplayName("PayslipDocumentServiceImpl")
class PayslipDocumentServiceImplTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("inlines tenant logo bytes when branding path points to a stored asset")
    void inlinesTenantLogoForStoredAssetPath() throws Exception {
        final TemplateEngine templateEngine = org.mockito.Mockito.mock(TemplateEngine.class);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html/>");

        final PayrollProperties properties = payrollProperties(tempDir);
        final Path logo = tempDir.resolve("ACME").resolve("logo.png");
        Files.createDirectories(logo.getParent());
        Files.write(logo, new byte[] {1, 2, 3, 4});

        final PayslipDocumentServiceImpl service = new PayslipDocumentServiceImpl(templateEngine, properties);
        service.renderPayslipPdfHtml(slipWithBannerPath("/api/v1/branding/assets/ACME/logo.png"));

        final ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(anyString(), contextCaptor.capture());
        final String bannerSrc = String.valueOf(contextCaptor.getValue().getVariable("bannerSrc"));
        assertThat(bannerSrc).startsWith("data:image/png;base64,");
    }

    @Test
    @DisplayName("does not read files outside tenant folder when branding path attempts traversal")
    void blocksPathTraversalForTenantLogoLookup() throws Exception {
        final TemplateEngine templateEngine = org.mockito.Mockito.mock(TemplateEngine.class);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html/>");

        final PayrollProperties properties = payrollProperties(tempDir);
        Files.writeString(tempDir.resolve("secret.txt"), "sensitive-data");

        final String traversalPath = "/api/v1/branding/assets/ACME/../secret.txt";
        final PayslipDocumentServiceImpl service = new PayslipDocumentServiceImpl(templateEngine, properties);
        service.renderPayslipPdfHtml(slipWithBannerPath(traversalPath));

        final ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(anyString(), contextCaptor.capture());
        final String bannerSrc = String.valueOf(contextCaptor.getValue().getVariable("bannerSrc"));
        assertThat(bannerSrc).isEqualTo(traversalPath);
    }

    private PayrollProperties payrollProperties(final Path storageRoot) {
        final PayrollProperties properties = new PayrollProperties();
        properties.getBrand().setCompanyName("Nexra");
        properties.getBrand().setBannerPath("/branding/nexra-banner.png");
        properties.getBrand().setWatermarkText("NEXRA");
        properties.getTenantBranding().setLogoStoragePath(storageRoot.toString());
        properties.getTenantBranding().setPublicLogoBasePath("/api/v1/branding/assets");
        properties.getSecurity().setJwtSecret("01234567890123456789012345678901");
        return properties;
    }

    private PayrollSlip slipWithBannerPath(final String bannerPath) {
        final OrganizationProfile organizationProfile = new OrganizationProfile(
            "ACME",
            "Acme",
            "Acme Legal",
            "Address 1",
            null,
            "Pune",
            "MH",
            "IN",
            "411001",
            "INR",
            new BigDecimal("10.00"),
            new BigDecimal("12.00"),
            "payroll@acme.test",
            "1234567890",
            bannerPath,
            "Acme Payroll",
            "CONFIDENTIAL",
            Instant.now(),
            "system"
        );

        final EmployeeProfile employeeProfile = new EmployeeProfile(
            "ACME",
            "EMP-1",
            "E001",
            "Alice Doe",
            "Engineering",
            "Engineer",
            new BigDecimal("100000.00"),
            "Bank",
            "XXXX1234",
            "AAAAA1234A",
            "XXXXXXXXXXXX",
            "alice@acme.test",
            Instant.now(),
            "system"
        );

        return new PayrollSlip(
            "SLIP-1",
            "ACME",
            "EMP-1",
            "E001",
            "Alice Doe",
            "Engineering",
            "Engineer",
            "2026-05",
            "INR",
            organizationProfile,
            employeeProfile,
            new BigDecimal("100000.00"),
            List.of(),
            List.of(),
            new BigDecimal("10.00"),
            new BigDecimal("12.00"),
            new BigDecimal("10000.00"),
            new BigDecimal("12000.00"),
            new BigDecimal("100000.00"),
            new BigDecimal("22000.00"),
            new BigDecimal("78000.00"),
            Instant.now(),
            "hr@acme.test",
            "user-1",
            new AuthDependencyStatus(true, "UP", "Auth reachable")
        );
    }
}
