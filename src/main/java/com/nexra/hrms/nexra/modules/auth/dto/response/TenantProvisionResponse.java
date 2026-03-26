package com.nexra.hrms.nexra.modules.auth.dto.response;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Defines TenantProvisionResponse component.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Getter
@Setter
public class TenantProvisionResponse {

    private String tenantCode;
    private String companyName;
    private String adminEmail;
    private Set<String> grantedProducts;
    private String message;
}
