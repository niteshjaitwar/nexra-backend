package com.nexra.hrms.nexra.modules.payroll.service;

import com.nexra.hrms.nexra.modules.payroll.dto.PayrollGenerationRequest;
import com.nexra.hrms.nexra.modules.payroll.dto.ProfilePayrollGenerationRequest;
import com.nexra.hrms.nexra.modules.payroll.model.AuthDependencyStatus;
import com.nexra.hrms.nexra.modules.payroll.model.PayrollSlip;
import com.nexra.hrms.nexra.modules.payroll.security.AuthenticatedPayrollUser;
import java.util.List;

/**
 * Defines payroll generation and payslip retrieval business operations.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public interface PayrollService {

    PayrollSlip generatePayroll(PayrollGenerationRequest request, AuthenticatedPayrollUser actor);

    PayrollSlip generatePayrollFromProfile(ProfilePayrollGenerationRequest request, AuthenticatedPayrollUser actor);

    PayrollSlip getSlip(String slipId);

    AuthDependencyStatus getAuthDependencyStatus();

    List<PayrollSlip> listSlipsForTenant(String tenantCode, AuthenticatedPayrollUser actor);
}
