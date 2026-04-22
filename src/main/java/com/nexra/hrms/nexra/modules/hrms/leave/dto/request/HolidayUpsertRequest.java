package com.nexra.hrms.nexra.modules.hrms.leave.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record HolidayUpsertRequest(
    String holidayId,
    @NotBlank @Size(max = 60) String tenantCode,
    @NotNull LocalDate holidayDate,
    @NotBlank @Size(max = 160) String name,
    @Size(max = 40) String locationCode,
    Boolean active
) {
}

