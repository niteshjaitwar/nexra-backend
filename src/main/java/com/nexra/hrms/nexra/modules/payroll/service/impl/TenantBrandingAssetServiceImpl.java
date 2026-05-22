package com.nexra.hrms.nexra.modules.payroll.service.impl;

import com.nexra.hrms.nexra.modules.payroll.config.PayrollProperties;
import com.nexra.hrms.nexra.modules.payroll.exception.PayrollResourceNotFoundException;
import java.awt.image.BufferedImage;
import com.nexra.hrms.nexra.modules.payroll.service.TenantBrandingAssetService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantBrandingAssetServiceImpl implements TenantBrandingAssetService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg");

    private final PayrollProperties payrollProperties;

    @Override
    public String storeTenantLogo(final String tenantCode, final MultipartFile logoFile) {
        if (logoFile == null || logoFile.isEmpty()) {
            throw new IllegalArgumentException("Logo file is required.");
        }
        if (logoFile.getSize() > payrollProperties.getTenantBranding().getMaxLogoBytes()) {
            throw new IllegalArgumentException("Logo file exceeds maximum allowed size.");
        }

        String extension = fileExtension(logoFile.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Logo file type is not supported.");
        }
        validateImagePayload(logoFile);

        String tenantFolder = sanitizeToken(tenantCode);
        String storedFileName = "logo-" + UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path directory = storageRoot().resolve(tenantFolder);
        Path destination = directory.resolve(storedFileName).normalize();
        try {
            Files.createDirectories(directory);
            try (InputStream in = logoFile.getInputStream()) {
                Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store tenant logo.", ex);
        }

        String basePath = payrollProperties.getTenantBranding().getPublicLogoBasePath();
        String publicPath = (basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath)
            + "/" + tenantFolder + "/" + storedFileName;
        log.info("TenantBrandingAssetService - tenant logo stored - tenantCode={}, publicPath={}", tenantCode, publicPath);
        return publicPath;
    }

    @Override
    public Resource loadLogo(final String tenantCode, final String filename) {
        String safeTenant = sanitizeToken(tenantCode);
        String safeFile = sanitizeFilename(filename);
        Path target = storageRoot().resolve(safeTenant).resolve(safeFile).normalize();
        if (!target.startsWith(storageRoot()) || !Files.exists(target) || !Files.isReadable(target)) {
            throw new PayrollResourceNotFoundException("Branding logo asset not found");
        }
        try {
            return new UrlResource(target.toUri());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read tenant logo asset.", ex);
        }
    }

    private Path storageRoot() {
        return Path.of(payrollProperties.getTenantBranding().getLogoStoragePath()).toAbsolutePath().normalize();
    }

    private String fileExtension(final String filename) {
        String clean = StringUtils.hasText(filename) ? filename.trim() : "";
        int idx = clean.lastIndexOf('.');
        if (idx < 0 || idx == clean.length() - 1) {
            return "";
        }
        return clean.substring(idx + 1).toLowerCase(Locale.ROOT);
    }

    private String sanitizeToken(final String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("Tenant code is required.");
        }
        return token.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private String sanitizeFilename(final String fileName) {
        if (!StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("Filename is required.");
        }
        return fileName.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private void validateImagePayload(final MultipartFile logoFile) {
        try (InputStream in = logoFile.getInputStream()) {
            BufferedImage image = ImageIO.read(in);
            if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
                throw new IllegalArgumentException("Logo file content is not a valid image.");
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Logo file content is not readable.", ex);
        }
    }
}
