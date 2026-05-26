package com.nexra.hrms.nexra.modules.payroll.service;

import com.nexra.hrms.nexra.common.audit.AuditEventService;
import com.nexra.hrms.nexra.modules.payroll.dto.PayrollGenerationRequest;
import com.nexra.hrms.nexra.modules.payroll.dto.PayrollLineItemRequest;
import com.nexra.hrms.nexra.modules.payroll.entity.PayrollSlipEntity;
import com.nexra.hrms.nexra.modules.payroll.model.AuthDependencyStatus;
import com.nexra.hrms.nexra.modules.payroll.model.PayrollSlip;
import com.nexra.hrms.nexra.modules.payroll.security.AuthenticatedPayrollUser;
import com.nexra.hrms.nexra.modules.payroll.service.impl.PayrollServiceImpl;
import com.nexra.hrms.nexra.modules.payroll.repository.PayrollSlipRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PayrollServiceTest {

    @Test
    void generatePayrollCalculatesGrossDeductionsAndNet() {
        AuthReferenceClient authReferenceClient = new AuthReferenceClient() {
            @Override
            public AuthDependencyStatus getAuthHealth() {
                return new AuthDependencyStatus(true, "UP", "stubbed");
            }
        };
        ProfileDirectoryService profileDirectoryService = mock(ProfileDirectoryService.class);
        PayrollSlipRepository payrollSlipRepository = mock(PayrollSlipRepository.class);
        AuditEventService auditEventService = mock(AuditEventService.class);
        when(payrollSlipRepository.save(any(PayrollSlipEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PayrollService payrollService = new PayrollServiceImpl(
            new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules(),
            authReferenceClient,
            profileDirectoryService,
            payrollSlipRepository,
            auditEventService
        );
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
            Set.of("ROLE_HR_ADMIN"),
            Set.of("PAYROLL")
        );

        PayrollSlip slip = payrollService.generatePayroll(request, actor);

        assertEquals(new BigDecimal("125000.00"), slip.grossEarnings());
        assertEquals(new BigDecimal("18950.00"), slip.totalDeductions());
        assertEquals(new BigDecimal("106050.00"), slip.netPay());

        ArgumentCaptor<PayrollSlipEntity> captor = ArgumentCaptor.forClass(PayrollSlipEntity.class);
        verify(payrollSlipRepository).save(captor.capture());
        assertNotNull(captor.getValue().getSlipId());
        assertEquals("TENANT1", captor.getValue().getTenantCode());
    }
}
