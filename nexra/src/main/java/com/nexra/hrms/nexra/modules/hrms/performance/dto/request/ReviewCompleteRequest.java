package com.nexra.hrms.nexra.modules.hrms.performance.dto.request; import jakarta.validation.constraints.*; import java.math.BigDecimal;
public record ReviewCompleteRequest(@NotBlank @Size(max=64) String tenantCode,@NotNull @DecimalMin("0.0") BigDecimal managerScore,@Size(max=2000) String managerComments) {}

