package com.nexra.hrms.nexra.modules.payroll.service;

import com.nexra.hrms.nexra.modules.payroll.model.PayrollSlip;

/**
 * Defines payslip HTML/PDF document rendering operations.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public interface PayslipDocumentService {

    String renderPayslipHtml(PayrollSlip slip);

    String renderPayslipPdfHtml(PayrollSlip slip);

    byte[] generateProtectedPdf(PayrollSlip slip);
}
