package com.nexra.hrms.nexra.modules.hrms.attendance.service;

import com.nexra.hrms.nexra.modules.hrms.attendance.dto.request.CheckInRequest;
import com.nexra.hrms.nexra.modules.hrms.attendance.dto.request.CheckOutRequest;
import com.nexra.hrms.nexra.modules.hrms.attendance.dto.request.ShiftUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.attendance.model.AttendanceRecordView;
import com.nexra.hrms.nexra.modules.hrms.attendance.model.ShiftView;
import com.nexra.hrms.nexra.modules.hrms.attendance.security.AuthenticatedAttendanceUser;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Defines attendance and shift management business operations with tenant-scoped access control.
 */
public interface AttendanceService {

    ShiftView upsertShift(ShiftUpsertRequest request, AuthenticatedAttendanceUser actor);

    List<ShiftView> listShifts(String tenantCode, boolean includeInactive, AuthenticatedAttendanceUser actor);

    AttendanceRecordView checkIn(CheckInRequest request, AuthenticatedAttendanceUser actor);

    AttendanceRecordView checkOut(CheckOutRequest request, AuthenticatedAttendanceUser actor);

    List<AttendanceRecordView> listRecords(
        String tenantCode,
        String employeeId,
        LocalDate fromDate,
        LocalDate toDate,
        AuthenticatedAttendanceUser actor
    );

    Map<String, Object> summary(
        String tenantCode,
        String employeeId,
        LocalDate fromDate,
        LocalDate toDate,
        AuthenticatedAttendanceUser actor
    );
}

