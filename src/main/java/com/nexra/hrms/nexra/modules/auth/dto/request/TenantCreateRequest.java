package com.nexra.hrms.nexra.modules.auth.dto.request;

import com.nexra.hrms.nexra.modules.auth.validation.TenantCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents data contract for TenantCreateRequest.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public record TenantCreateRequest(
    @NotBlank @Size(max = 60) @TenantCode String code,
    @NotBlank @Size(max = 120) String name,
    boolean enterprise
) {
}
