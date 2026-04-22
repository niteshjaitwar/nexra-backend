package com.nexra.hrms.nexra.modules.hrms.leave.service;

import com.nexra.hrms.nexra.modules.hrms.leave.dto.request.HolidayUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.leave.dto.request.LeaveBalanceAdjustRequest;
import com.nexra.hrms.nexra.modules.hrms.leave.dto.request.LeaveDecisionRequest;
import com.nexra.hrms.nexra.modules.hrms.leave.dto.request.LeaveRequestCreateRequest;
import com.nexra.hrms.nexra.modules.hrms.leave.dto.request.LeaveTypeUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.leave.model.Holiday;
import com.nexra.hrms.nexra.modules.hrms.leave.model.LeaveBalance;
import com.nexra.hrms.nexra.modules.hrms.leave.model.LeaveRequestView;
import com.nexra.hrms.nexra.modules.hrms.leave.model.LeaveType;
import com.nexra.hrms.nexra.modules.hrms.leave.security.AuthenticatedLeaveUser;
import java.time.LocalDate;
import java.util.List;

/**
 * Defines tenant-scoped leave and holiday management business operations.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public interface LeaveManagementService {

    LeaveType upsertLeaveType(LeaveTypeUpsertRequest request, AuthenticatedLeaveUser actor);

    List<LeaveType> listLeaveTypes(String tenantCode, boolean includeInactive, AuthenticatedLeaveUser actor);

    Holiday upsertHoliday(HolidayUpsertRequest request, AuthenticatedLeaveUser actor);

    List<Holiday> listHolidays(String tenantCode, LocalDate fromDate, LocalDate toDate, AuthenticatedLeaveUser actor);

    LeaveBalance adjustBalance(LeaveBalanceAdjustRequest request, AuthenticatedLeaveUser actor);

    List<LeaveBalance> listBalances(String tenantCode, String employeeId, AuthenticatedLeaveUser actor);

    LeaveRequestView createLeaveRequest(LeaveRequestCreateRequest request, AuthenticatedLeaveUser actor);

    List<LeaveRequestView> listLeaveRequests(String tenantCode, String employeeId, String status, AuthenticatedLeaveUser actor);

    LeaveRequestView getLeaveRequest(String tenantCode, String requestId, AuthenticatedLeaveUser actor);

    LeaveRequestView approveLeaveRequest(String requestId, LeaveDecisionRequest request, AuthenticatedLeaveUser actor);

    LeaveRequestView rejectLeaveRequest(String requestId, LeaveDecisionRequest request, AuthenticatedLeaveUser actor);
}

