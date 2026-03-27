package com.nexra.hrms.nexra.modules.auth.dto.response;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Defines TenantResponse component.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Getter
@Setter
public class TenantResponse {

    private UUID id;
    private String code;
    private String name;
    private boolean enterprise;
    private boolean active;
}
