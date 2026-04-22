package com.nexra.hrms.nexra.modules.auth.controller.dev;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.dev.DevDbStatsResponse;
import com.nexra.hrms.nexra.modules.auth.repository.RefreshTokenRepository;
import com.nexra.hrms.nexra.modules.auth.repository.TenantRepository;
import com.nexra.hrms.nexra.modules.auth.repository.UserAccountRepository;
import com.nexra.hrms.nexra.modules.auth.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes development-only diagnostics endpoints for local validation.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Slf4j
@RestController
@Profile({"dev", "e2e"})
@RequiredArgsConstructor
@RequestMapping("/api/v1/dev")
public class DevDiagnosticsController {

    private final TenantRepository tenantRepository;
    private final UserAccountRepository userAccountRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Returns current table-level counts to verify local database state.
     *
     * @return standardized API response with database counters
     */
    @GetMapping("/db-stats")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<DevDbStatsResponse>> dbStats() {
        log.info("DevDiagnosticsController() - dbStats() - Fetching dev database statistics");
        DevDbStatsResponse stats = new DevDbStatsResponse(
            tenantRepository.count(),
            userAccountRepository.count(),
            verificationTokenRepository.count(),
            refreshTokenRepository.count()
        );
        return ResponseEntity.ok(ApiResponse.success("Dev DB stats fetched successfully.", stats));
    }
}
