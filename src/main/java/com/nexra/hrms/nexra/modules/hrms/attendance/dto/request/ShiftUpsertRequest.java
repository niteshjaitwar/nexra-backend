package com.nexra.hrms.nexra.modules.hrms.attendance.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ShiftUpsertRequest(
    @NotBlank @Size(max = 60) String tenantCode,
    @NotBlank @Size(max = 40) String code,
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Pattern(regexp = "^\\d{2}:\\d{2}$") String startTime,
    @NotBlank @Pattern(regexp = "^\\d{2}:\\d{2}$") String endTime,
    @NotNull @Min(0) @Max(180) Integer graceMinutes,
    Boolean active
) {
}

