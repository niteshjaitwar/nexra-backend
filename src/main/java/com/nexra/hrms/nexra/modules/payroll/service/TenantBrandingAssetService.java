package com.nexra.hrms.nexra.modules.payroll.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Handles secure storage and retrieval of tenant branding logo assets.
 */
public interface TenantBrandingAssetService {

    String storeTenantLogo(String tenantCode, MultipartFile logoFile);

    Resource loadLogo(String tenantCode, String filename);
}

