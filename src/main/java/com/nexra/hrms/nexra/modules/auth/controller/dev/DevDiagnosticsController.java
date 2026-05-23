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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Exposes development-only diagnostics endpoints for local validation.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Slf4j
@Tag(name = "Authentication Diagnostics", description = "Authentication diagnostics APIs.")
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
    @Operation(summary = "GET /api/v1/dev/db-stats", description = "Processes GET requests for /api/v1/dev/db-stats.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
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
