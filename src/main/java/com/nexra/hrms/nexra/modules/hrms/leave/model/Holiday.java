package com.nexra.hrms.nexra.modules.hrms.leave.model;

import java.time.Instant;
import java.time.LocalDate;

public record Holiday(
    String holidayId,
    String tenantCode,
    LocalDate holidayDate,
    String name,
    String locationCode,
    boolean active,
    Instant updatedAt,
    String updatedBy
) {
}

