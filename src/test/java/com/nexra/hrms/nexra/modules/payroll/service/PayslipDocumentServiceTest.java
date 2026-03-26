package com.nexra.hrms.nexra.modules.payroll.service;

import com.nexra.hrms.nexra.modules.payroll.model.AuthDependencyStatus;
import com.nexra.hrms.nexra.modules.payroll.model.PayrollSlip;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class PayslipDocumentServiceTest {

    @Autowired
    private PayslipDocumentService payslipDocumentService;

    @Test
    void generatesPdfBytes() {
        PayrollSlip slip = new PayrollSlip(
            "SLIP-1",
            "TENANT1",
            "EMP-1",
            "NXR-001",
            "Aarav Singh",
            "Engineering",
            "Software Engineer",
            "2026-02",
            "INR",
            null,
            null,
            new BigDecimal("100000.00"),
            List.of(),
            List.of(),
            new BigDecimal("10.00"),
            new BigDecimal("5.00"),
            new BigDecimal("10000.00"),
            new BigDecimal("5000.00"),
            new BigDecimal("100000.00"),
            new BigDecimal("15000.00"),
            new BigDecimal("85000.00"),
            Instant.parse("2026-02-22T10:00:00Z"),
            "hr@nexra.local",
            "user-1",
            new AuthDependencyStatus(true, "UP", "stub")
        );

        byte[] pdf = payslipDocumentService.generateProtectedPdf(slip);

        assertTrue(pdf.length > 1000);
        assertTrue(new String(pdf, 0, 4, StandardCharsets.US_ASCII).startsWith("%PDF"));
    }
}
