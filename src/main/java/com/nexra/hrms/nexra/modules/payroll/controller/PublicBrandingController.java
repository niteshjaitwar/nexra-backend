package com.nexra.hrms.nexra.modules.payroll.controller;

import com.nexra.hrms.nexra.modules.payroll.config.PayrollProperties;
import com.nexra.hrms.nexra.modules.payroll.dto.ApiResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/branding")
@Slf4j
public class PublicBrandingController {

    private final PayrollProperties payrollProperties;

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
}
