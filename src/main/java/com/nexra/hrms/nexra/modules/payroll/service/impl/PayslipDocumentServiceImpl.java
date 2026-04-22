package com.nexra.hrms.nexra.modules.payroll.service.impl;

import com.nexra.hrms.nexra.modules.payroll.config.PayrollProperties;
import com.nexra.hrms.nexra.modules.payroll.model.PayrollSlip;
import com.nexra.hrms.nexra.modules.payroll.service.PayslipDocumentService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Renders payroll payslip HTML/PDF documents and applies PDF protection.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayslipDocumentServiceImpl implements PayslipDocumentService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String PNG_MIME_TYPE = "image/png";
    private static final String JPEG_MIME_TYPE = "image/jpeg";
    private static final String WEBP_MIME_TYPE = "image/webp";

    private final TemplateEngine templateEngine;
    private final PayrollProperties payrollProperties;

    @Override
    public String renderPayslipHtml(final PayrollSlip slip) {
        return renderTemplate("payslip", slip);
    }

    @Override
    public String renderPayslipPdfHtml(final PayrollSlip slip) {
        return renderTemplate("payslip-pdf", slip);
    }

    private String renderTemplate(final String templateName, final PayrollSlip slip) {
        Context context = new Context();
        Map<String, Object> variables = new HashMap<>();
        variables.put("slip", slip);
        variables.put("brand", payrollProperties.getBrand());
        variables.put("bannerSrc", inlineBannerDataUri());
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

    @Override
    public byte[] generateProtectedPdf(final PayrollSlip slip) {
        String html = renderPayslipPdfHtml(slip);
        try (ByteArrayOutputStream rawPdf = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(rawPdf);
            builder.run();
            return applyProtection(rawPdf.toByteArray());
        } catch (Exception ex) {
            log.error("PayslipDocumentServiceImpl - generateProtectedPdf failed for slipId={}", slip.slipId(), ex);
            throw new IllegalStateException("Failed to generate payslip PDF", ex);
        }
    }

    private byte[] applyProtection(final byte[] rawPdf) throws IOException {
        try (
            PDDocument document = PDDocument.load(rawPdf);
            ByteArrayOutputStream secured = new ByteArrayOutputStream()
        ) {
            AccessPermission permission = new AccessPermission();
            permission.setCanModify(false);
            permission.setCanModifyAnnotations(false);
            permission.setCanAssembleDocument(false);
            permission.setCanFillInForm(false);
            permission.setCanExtractForAccessibility(true);
            permission.setCanExtractContent(false);
            permission.setCanPrint(true);

            String ownerPassword = randomOwnerPassword();
            StandardProtectionPolicy protectionPolicy = new StandardProtectionPolicy(ownerPassword, "", permission);
            protectionPolicy.setEncryptionKeyLength(256);
            protectionPolicy.setPermissions(permission);
            document.protect(protectionPolicy);
            document.save(secured);
            return secured.toByteArray();
        }
    }

    private String randomOwnerPassword() {
        byte[] bytes = new byte[24];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String inlineBannerDataUri() {
        try {
            String bannerPath = payrollProperties.getBrand().getBannerPath();
            if (bannerPath == null || bannerPath.isBlank()) {
                return "";
            }
            String classpathPath = "static" + (bannerPath.startsWith("/") ? bannerPath : "/" + bannerPath);
            try (InputStream stream = new ClassPathResource(classpathPath).getInputStream()) {
                byte[] bytes = stream.readAllBytes();
                String mimeType = detectMimeType(bannerPath);
                return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(bytes);
            }
        } catch (IOException ex) {
            log.warn("PayslipDocumentServiceImpl - inlineBannerDataUri fallback to path due to error: {}", ex.getMessage());
            return payrollProperties.getBrand().getBannerPath();
        }
    }

    private String detectMimeType(final String bannerPath) {
        final String normalized = bannerPath.toLowerCase();
        if (normalized.endsWith(".jpg") || normalized.endsWith(".jpeg")) {
            return JPEG_MIME_TYPE;
        }
        if (normalized.endsWith(".webp")) {
            return WEBP_MIME_TYPE;
        }
        return PNG_MIME_TYPE;
    }
}
