package com.nexra.hrms.nexra.modules.payroll.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * API representation of a generated statutory filing artifact.
 *
 * @param id                    filing identifier.
 * @param tenantCode            owning tenant.
 * @param countryCode           statutory country.
 * @param period                pay period (YYYY-MM).
 * @param filingType            jurisdiction filing type.
 * @param referenceNumber       generated filing reference.
 * @param status                filing status.
 * @param employeeCount         number of employees included.
 * @param totalGross            aggregate gross wages.
 * @param employeeContribution  aggregate employee-borne contributions.
 * @param employerContribution  aggregate employer-borne contributions.
 * @param totalContribution     combined contributions.
 * @param componentTotals       per-component aggregated totals.
 * @param generatedAt           generation timestamp.
 */
public record PayrollStatutoryFiling(
    String id,
    String tenantCode,
    String countryCode,
    String period,
    String filingType,
    String referenceNumber,
    String status,
    int employeeCount,
    BigDecimal totalGross,
    BigDecimal employeeContribution,
    BigDecimal employerContribution,
    BigDecimal totalContribution,
    List<ComponentTotal> componentTotals,
    Instant generatedAt
) {

    /**
     * Aggregated total for a single statutory component across all employees.
     *
     * @param componentCode component identifier.
     * @param employerBorne whether the employer bears this component.
     * @param amount        aggregated amount.
     */
    public record ComponentTotal(String componentCode, boolean employerBorne, BigDecimal amount) {
    }
}
