package com.nexra.hrms.nexra.modules.payroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to mark a statutory filing as submitted to authority.
 */
public record PayrollStatutoryFilingSubmitRequest(
    @Size(max = 500) String submissionReference
) {
}
