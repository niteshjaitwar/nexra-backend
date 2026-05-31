package com.nexra.hrms.nexra.modules.payroll.service;

import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.modules.payroll.entity.PayrollStatutoryComponentEntity;
import com.nexra.hrms.nexra.modules.payroll.entity.PayrollStatutorySlabEntity;
import com.nexra.hrms.nexra.modules.payroll.repository.PayrollStatutoryComponentRepository;
import com.nexra.hrms.nexra.modules.payroll.repository.PayrollStatutorySlabRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Applies tenant-scoped statutory payroll components loaded from Flyway-managed
 * reference data. Supports percentage components with wage ceilings, fixed
 * amounts, slab-driven (band) components, eligibility windows, and a split
 * between employee-borne and employer-borne contributions.
 *
 * <p>All monetary maths use {@link BigDecimal} with HALF_UP rounding to two
 * decimal places, as required for money calculations.
 *
 * @author niteshjaitwar
 */
@Service
@RequiredArgsConstructor
public class PayrollStatutoryService {

    public static final String PLATFORM_TENANT_CODE = "__PLATFORM__";

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final PayrollStatutoryComponentRepository componentRepository;
    private final PayrollStatutorySlabRepository slabRepository;

    /**
     * Returns the per-component statutory amount map (employee and employer
     * contributions keyed by component code).
     *
     * @param tenantCode   tenant requesting the calculation.
     * @param countryCode  statutory country code.
     * @param grossMonthly gross monthly wage.
     * @return immutable map of component code to calculated amount.
     */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> calculateStatutory(
        final String tenantCode,
        final String countryCode,
        final BigDecimal grossMonthly
    ) {
        final StatutoryBreakdown breakdown = calculateBreakdown(tenantCode, countryCode, grossMonthly);
        final Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (final StatutoryComponentAmount component : breakdown.components()) {
            result.put(component.componentCode(), component.amount());
        }
        return Map.copyOf(result);
    }

    /**
     * Computes the full statutory breakdown including employee total, employer
     * total, and total employment cost contribution.
     *
     * @param tenantCode   tenant requesting the calculation.
     * @param countryCode  statutory country code.
     * @param grossMonthly gross monthly wage.
     * @return structured statutory breakdown.
     */
    @Transactional(readOnly = true)
    public StatutoryBreakdown calculateBreakdown(
        final String tenantCode,
        final String countryCode,
        final BigDecimal grossMonthly
    ) {
        final String normalizedTenant = normalize(tenantCode);
        final String normalizedCountry = normalizeCountry(countryCode);
        final BigDecimal gross = grossMonthly == null ? BigDecimal.ZERO : grossMonthly.max(BigDecimal.ZERO);

        List<PayrollStatutoryComponentEntity> components = componentRepository
            .findAllByTenantCodeIgnoreCaseAndCountryCodeIgnoreCaseAndActiveTrueOrderByComponentCodeAsc(
                normalizedTenant,
                normalizedCountry
            );
        String resolvedTenant = normalizedTenant;
        if (components.isEmpty()) {
            resolvedTenant = PLATFORM_TENANT_CODE;
            components = componentRepository
                .findAllByTenantCodeIgnoreCaseAndCountryCodeIgnoreCaseAndActiveTrueOrderByComponentCodeAsc(
                    PLATFORM_TENANT_CODE,
                    normalizedCountry
                );
        }
        if (components.isEmpty()) {
            throw new NexraValidationException("No active statutory components configured for country: " + normalizedCountry);
        }

        final List<StatutoryComponentAmount> details = new ArrayList<>();
        BigDecimal employeeTotal = BigDecimal.ZERO;
        BigDecimal employerTotal = BigDecimal.ZERO;
        for (final PayrollStatutoryComponentEntity component : components) {
            final BigDecimal amount = calculateComponentAmount(component, gross, resolvedTenant, normalizedCountry);
            details.add(new StatutoryComponentAmount(
                component.getComponentCode(),
                component.getComponentName(),
                component.getComponentType() == null ? "" : component.getComponentType().toUpperCase(Locale.ROOT),
                component.isEmployerBorne(),
                amount
            ));
            if (component.isEmployerBorne()) {
                employerTotal = employerTotal.add(amount);
            } else {
                employeeTotal = employeeTotal.add(amount);
            }
        }

        return new StatutoryBreakdown(
            normalizedCountry,
            scale(gross),
            scale(employeeTotal),
            scale(employerTotal),
            scale(employeeTotal.add(employerTotal)),
            List.copyOf(details)
        );
    }

    private BigDecimal calculateComponentAmount(
        final PayrollStatutoryComponentEntity component,
        final BigDecimal gross,
        final String tenantCode,
        final String countryCode
    ) {
        if (!isEligible(component, gross)) {
            return scale(BigDecimal.ZERO);
        }
        final String type = component.getComponentType() == null
            ? ""
            : component.getComponentType().trim().toUpperCase(Locale.ROOT);
        return switch (type) {
            case "PERCENTAGE", "RATE" -> {
                if (component.getRatePercent() == null) {
                    throw new NexraValidationException("Percentage component missing rate: " + component.getComponentCode());
                }
                final BigDecimal base = applyBand(gross, component.getWageFloor(), component.getWageCeiling());
                yield scale(base.multiply(component.getRatePercent()).divide(HUNDRED, 2, RoundingMode.HALF_UP));
            }
            case "FIXED" -> {
                if (component.getFixedAmount() == null) {
                    throw new NexraValidationException("Fixed component missing amount: " + component.getComponentCode());
                }
                yield scale(component.getFixedAmount());
            }
            case "SLAB" -> resolveSlabAmount(component, gross, tenantCode, countryCode);
            default -> throw new NexraValidationException("Unsupported statutory component type: " + type);
        };
    }

    private BigDecimal resolveSlabAmount(
        final PayrollStatutoryComponentEntity component,
        final BigDecimal gross,
        final String tenantCode,
        final String countryCode
    ) {
        final List<PayrollStatutorySlabEntity> slabs = slabRepository
            .findAllByTenantCodeIgnoreCaseAndCountryCodeIgnoreCaseAndComponentCodeIgnoreCaseAndActiveTrue(
                tenantCode,
                countryCode,
                component.getComponentCode()
            );
        if (slabs.isEmpty()) {
            throw new NexraValidationException("No active slabs configured for component: " + component.getComponentCode());
        }
        return slabs.stream()
            .filter((slab) -> withinRange(gross, slab.getMinGross(), slab.getMaxGross()))
            .map((slab) -> scale(slab.getFixedAmount()))
            .findFirst()
            .orElse(scale(BigDecimal.ZERO));
    }

    private boolean isEligible(final PayrollStatutoryComponentEntity component, final BigDecimal gross) {
        return withinRange(gross, component.getMinGross(), component.getMaxGross());
    }

    private boolean withinRange(final BigDecimal value, final BigDecimal min, final BigDecimal max) {
        if (min != null && value.compareTo(min) < 0) {
            return false;
        }
        return max == null || value.compareTo(max) <= 0;
    }

    /**
     * Resolves the contribution base for a percentage component. The base is the
     * gross capped at the wage ceiling, less any exempt earnings floor (the band
     * is never negative).
     */
    private BigDecimal applyBand(final BigDecimal gross, final BigDecimal floor, final BigDecimal ceiling) {
        final BigDecimal capped = ceiling == null ? gross : gross.min(ceiling);
        if (floor == null) {
            return capped;
        }
        return capped.subtract(floor).max(BigDecimal.ZERO);
    }

    private BigDecimal scale(final BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalize(final String value) {
        if (value == null || value.isBlank()) {
            throw new NexraValidationException("Tenant code is required.");
        }
        return value.trim();
    }

    private String normalizeCountry(final String value) {
        if (value == null || value.isBlank()) {
            throw new NexraValidationException("Country code is required.");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Structured statutory result split by who bears the contribution.
     *
     * @param countryCode   statutory country.
     * @param grossMonthly  gross wage used for the calculation.
     * @param employeeTotal sum of employee-borne contributions.
     * @param employerTotal sum of employer-borne contributions.
     * @param totalContribution combined employee and employer contributions.
     * @param components    per-component detail lines.
     */
    public record StatutoryBreakdown(
        String countryCode,
        BigDecimal grossMonthly,
        BigDecimal employeeTotal,
        BigDecimal employerTotal,
        BigDecimal totalContribution,
        List<StatutoryComponentAmount> components
    ) {
    }

    /**
     * A single calculated statutory component line.
     *
     * @param componentCode component identifier.
     * @param componentName human-readable component name.
     * @param componentType PERCENTAGE, FIXED, or SLAB.
     * @param employerBorne true when the employer bears the contribution.
     * @param amount        calculated contribution amount.
     */
    public record StatutoryComponentAmount(
        String componentCode,
        String componentName,
        String componentType,
        boolean employerBorne,
        BigDecimal amount
    ) {
    }
}
