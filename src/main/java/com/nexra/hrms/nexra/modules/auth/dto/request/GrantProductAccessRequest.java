package com.nexra.hrms.nexra.modules.auth.dto.request;

import com.nexra.hrms.nexra.modules.auth.enums.ProductRole;
import com.nexra.hrms.nexra.modules.auth.enums.ProductType;
import jakarta.validation.constraints.NotNull;

/**
 * Represents data contract for GrantProductAccessRequest.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public record GrantProductAccessRequest(
    @NotNull ProductType product,
    @NotNull ProductRole productRole
) {
}
