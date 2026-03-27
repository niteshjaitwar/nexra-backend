package com.nexra.hrms.nexra.modules.auth.dto.response;

import com.nexra.hrms.nexra.modules.auth.enums.ProductRole;
import com.nexra.hrms.nexra.modules.auth.enums.ProductType;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Defines ProductAccessResponse component.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Getter
@Setter
public class ProductAccessResponse {

    private UUID id;
    private UUID userId;
    private ProductType product;
    private ProductRole productRole;
    private Instant grantedAt;
    private String grantedBy;
}
