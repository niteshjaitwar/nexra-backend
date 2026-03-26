package com.nexra.hrms.nexra.modules.payroll.service;

import com.nexra.hrms.nexra.modules.payroll.dto.PayrollGenerationRequest;
import com.nexra.hrms.nexra.modules.payroll.dto.PayrollLineItemRequest;
import com.nexra.hrms.nexra.modules.payroll.model.AuthDependencyStatus;
import com.nexra.hrms.nexra.modules.payroll.model.PayrollSlip;
import com.nexra.hrms.nexra.modules.payroll.security.AuthenticatedPayrollUser;
import com.nexra.hrms.nexra.modules.payroll.service.impl.PayrollServiceImpl;
import com.nexra.hrms.nexra.modules.payroll.service.impl.ProfileDirectoryServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayrollServiceTest {

    @Test
    void generatePayrollCalculatesGrossDeductionsAndNet() {
        AuthReferenceClient authReferenceClient = new AuthReferenceClient() {
            @Override
            public AuthDependencyStatus getAuthHealth() {
                return new AuthDependencyStatus(true, "UP", "stubbed");
            }
        };
        ProfileDirectoryService profileDirectoryService = new ProfileDirectoryServiceImpl();

        PayrollService payrollService = new PayrollServiceImpl(authReferenceClient, profileDirectoryService);
        PayrollGenerationRequest request = new PayrollGenerationRequest(
            "EMP-1",
            "NXR-001",
            "Aarav Singh",
            "TENANT1",
            "Engineering",
            "Software Engineer",
            "2026-02",
            new BigDecimal("100000"),
            List.of(new PayrollLineItemRequest("HRA", new BigDecimal("25000"))),
            List.of(new PayrollLineItemRequest("Professional Tax", new BigDecimal("200"))),
            new BigDecimal("10"),
            new BigDecimal("5"),
            "INR"
        );
        AuthenticatedPayrollUser actor = new AuthenticatedPayrollUser(
            UUID.randomUUID(),
            "hr@nexra.local",
            "TENANT1",
            Set.of("ROLE_HR_ADMIN")
        );

        PayrollSlip slip = payrollService.generatePayroll(request, actor);

        assertEquals(new BigDecimal("125000.00"), slip.grossEarnings());
        assertEquals(new BigDecimal("18950.00"), slip.totalDeductions());
        assertEquals(new BigDecimal("106050.00"), slip.netPay());
    }
}
