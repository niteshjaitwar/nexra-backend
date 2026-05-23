package com.nexra.hrms.nexra.modules.payroll.controller;

import com.nexra.hrms.nexra.modules.payroll.config.PayrollProperties;
import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.modules.payroll.model.OrganizationProfile;
import com.nexra.hrms.nexra.modules.payroll.service.ProfileDirectoryService;
import com.nexra.hrms.nexra.modules.payroll.service.TenantBrandingAssetService;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Payroll", description = "Payroll APIs.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/branding")
@Slf4j
public class PublicBrandingController {

    private final PayrollProperties payrollProperties;
    private final ProfileDirectoryService profileDirectoryService;
    private final TenantBrandingAssetService tenantBrandingAssetService;

    @Operation(summary = "GET endpoint", description = "Handles GET requests for this resource.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> branding() {
        log.debug("PublicBrandingController - branding metadata requested");
        Map<String, String> payload = Map.of(
            "companyName", payrollProperties.getBrand().getCompanyName(),
            "bannerPath", payrollProperties.getBrand().getBannerPath(),
            "watermarkText", payrollProperties.getBrand().getWatermarkText()
        );
        return ResponseEntity.ok(ApiResponse.success("Branding metadata fetched.", payload));
    }

    @Operation(summary = "GET endpoint", description = "Handles GET requests for this resource.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/{tenantCode}")
    public ResponseEntity<ApiResponse<Map<String, String>>> tenantBranding(@PathVariable final String tenantCode) {
        OrganizationProfile profile = profileDirectoryService.getOrganizationProfileInternal(tenantCode);
        Map<String, String> payload = Map.of(
            "tenantCode", tenantCode,
            "companyName", profile.brandingCompanyName() == null
                ? payrollProperties.getBrand().getCompanyName() : profile.brandingCompanyName(),
            "bannerPath", profile.brandingLogoPath() == null
                ? payrollProperties.getBrand().getBannerPath() : profile.brandingLogoPath(),
            "watermarkText", profile.brandingWatermarkText() == null
                ? payrollProperties.getBrand().getWatermarkText() : profile.brandingWatermarkText()
        );
        return ResponseEntity.ok(ApiResponse.success("Tenant branding metadata fetched.", payload));
    }

    @Operation(summary = "GET endpoint", description = "Handles GET requests for this resource.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/assets/{tenantCode}/{filename:.+}")
    public ResponseEntity<Resource> tenantBrandingAsset(
        @PathVariable final String tenantCode,
        @PathVariable final String filename
    ) {
        Resource resource = tenantBrandingAssetService.loadLogo(tenantCode, filename);
        MediaType mediaType = detectMediaType(filename);
        return ResponseEntity.ok()
            .contentType(mediaType)
            .body(resource);
    }

    private MediaType detectMediaType(final String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        }
        if (lower.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
