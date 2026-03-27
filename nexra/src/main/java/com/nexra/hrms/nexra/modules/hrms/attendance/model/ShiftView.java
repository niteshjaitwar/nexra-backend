package com.nexra.hrms.nexra.modules.hrms.attendance.model;

import java.time.Instant;

public record ShiftView(
    String shiftId,
    String tenantCode,
    String code,
    String name,
    String startTime,
    String endTime,
    int graceMinutes,
    boolean active,
    Instant updatedAt,
    String updatedBy
) {
}

