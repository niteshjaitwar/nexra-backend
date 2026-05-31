package com.nexra.hrms.nexra.modules.payroll.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.nexra.hrms.nexra.modules.payroll.entity.PayrollStatutoryComponentEntity;
import com.nexra.hrms.nexra.modules.payroll.entity.PayrollStatutorySlabEntity;
import com.nexra.hrms.nexra.modules.payroll.repository.PayrollStatutoryComponentRepository;
import com.nexra.hrms.nexra.modules.payroll.repository.PayrollStatutorySlabRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PayrollStatutoryServiceTest {

    @Mock
    private PayrollStatutoryComponentRepository componentRepository;

    @Mock
    private PayrollStatutorySlabRepository slabRepository;

    @InjectMocks
    private PayrollStatutoryService service;

    @Test
    void calculatesFromConfiguredComponents() {
        when(componentRepository.findAllByTenantCodeIgnoreCaseAndCountryCodeIgnoreCaseAndActiveTrueOrderByComponentCodeAsc("ACME", "IN"))
            .thenReturn(List.of());
        when(componentRepository.findAllByTenantCodeIgnoreCaseAndCountryCodeIgnoreCaseAndActiveTrueOrderByComponentCodeAsc(
            PayrollStatutoryService.PLATFORM_TENANT_CODE,
            "IN"
        )).thenReturn(List.of(pfComponent(), esiComponent(), ptComponent()));

        final var result = service.calculateStatutory("ACME", "IN", new BigDecimal("100000"));
        assertThat(result.get("PF_EMPLOYEE")).isEqualByComparingTo("12000.00");
        assertThat(result.get("ESI_EMPLOYEE")).isEqualByComparingTo("750.00");
        assertThat(result.get("PROFESSIONAL_TAX")).isEqualByComparingTo("200.00");
    }

    @Test
    void appliesWageCeilingEligibilityAndEmployerSplit() {
        final PayrollStatutoryComponentEntity pfEmployee = pfComponent();
        pfEmployee.setWageCeiling(new BigDecimal("15000.00"));
        final PayrollStatutoryComponentEntity pfEmployer = component("PF_EMPLOYER", "PERCENTAGE", new BigDecimal("12.0000"), null);
        pfEmployer.setEmployerBorne(true);
        pfEmployer.setWageCeiling(new BigDecimal("15000.00"));
        final PayrollStatutoryComponentEntity esiEmployee = esiComponent();
        esiEmployee.setMaxGross(new BigDecimal("21000.00"));

        when(componentRepository.findAllByTenantCodeIgnoreCaseAndCountryCodeIgnoreCaseAndActiveTrueOrderByComponentCodeAsc("ACME", "IN"))
            .thenReturn(List.of(pfEmployee, pfEmployer, esiEmployee));

        final var breakdown = service.calculateBreakdown("ACME", "IN", new BigDecimal("100000"));

        // PF base is capped at the 15000 ceiling -> 12% = 1800 (employee and employer).
        assertThat(breakdown.employeeTotal()).isEqualByComparingTo("1800.00");
        assertThat(breakdown.employerTotal()).isEqualByComparingTo("1800.00");
        assertThat(breakdown.totalContribution()).isEqualByComparingTo("3600.00");
        // ESI is not eligible at 100000 gross (above 21000 threshold).
        assertThat(breakdown.components().stream()
            .filter((c) -> c.componentCode().equals("ESI_EMPLOYEE"))
            .findFirst().orElseThrow().amount()).isEqualByComparingTo("0.00");
    }

    @Test
    void resolvesSlabDrivenComponent() {
        final PayrollStatutoryComponentEntity pt = component("PROFESSIONAL_TAX", "SLAB", null, null);
        when(componentRepository.findAllByTenantCodeIgnoreCaseAndCountryCodeIgnoreCaseAndActiveTrueOrderByComponentCodeAsc("ACME", "IN"))
            .thenReturn(List.of(pt));
        when(slabRepository.findAllByTenantCodeIgnoreCaseAndCountryCodeIgnoreCaseAndComponentCodeIgnoreCaseAndActiveTrue("ACME", "IN", "PROFESSIONAL_TAX"))
            .thenReturn(List.of(slab("0.00", "14999.99", "0.00"), slab("15000.00", null, "200.00")));

        final var low = service.calculateStatutory("ACME", "IN", new BigDecimal("10000"));
        assertThat(low.get("PROFESSIONAL_TAX")).isEqualByComparingTo("0.00");

        final var high = service.calculateStatutory("ACME", "IN", new BigDecimal("50000"));
        assertThat(high.get("PROFESSIONAL_TAX")).isEqualByComparingTo("200.00");
    }

    private PayrollStatutoryComponentEntity component(
        final String code,
        final String type,
        final BigDecimal rate,
        final BigDecimal fixed
    ) {
        final PayrollStatutoryComponentEntity entity = new PayrollStatutoryComponentEntity();
        entity.setComponentCode(code);
        entity.setComponentName(code);
        entity.setComponentType(type);
        entity.setRatePercent(rate);
        entity.setFixedAmount(fixed);
        return entity;
    }

    private PayrollStatutorySlabEntity slab(final String min, final String max, final String amount) {
        final PayrollStatutorySlabEntity slab = new PayrollStatutorySlabEntity();
        slab.setComponentCode("PROFESSIONAL_TAX");
        slab.setMinGross(new BigDecimal(min));
        slab.setMaxGross(max == null ? null : new BigDecimal(max));
        slab.setFixedAmount(new BigDecimal(amount));
        return slab;
    }

    private PayrollStatutoryComponentEntity pfComponent() {
        return component("PF_EMPLOYEE", "PERCENTAGE", new BigDecimal("12.0000"), null);
    }

    private PayrollStatutoryComponentEntity esiComponent() {
        return component("ESI_EMPLOYEE", "PERCENTAGE", new BigDecimal("0.7500"), null);
    }

    private PayrollStatutoryComponentEntity ptComponent() {
        return component("PROFESSIONAL_TAX", "FIXED", null, new BigDecimal("200.00"));
    }
}
