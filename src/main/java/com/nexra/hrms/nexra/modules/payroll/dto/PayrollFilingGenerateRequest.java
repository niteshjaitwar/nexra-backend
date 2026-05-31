package com.nexra.hrms.nexra.modules.payroll.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

/**
 * Request to generate a statutory filing artifact for a pay period.
 *
 * @param period       pay period in YYYY-MM format.
 * @param grossAmounts per-employee gross monthly wages included in the filing.
 */
public record PayrollFilingGenerateRequest(
    @NotNull
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "period must be in YYYY-MM format")
    String period,

    @NotEmpty(message = "at least one gross amount is required")
    List<@NotNull @PositiveOrZero BigDecimal> grossAmounts
) {
}
