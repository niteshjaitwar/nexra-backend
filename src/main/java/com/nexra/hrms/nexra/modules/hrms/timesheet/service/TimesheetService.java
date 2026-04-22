package com.nexra.hrms.nexra.modules.hrms.timesheet.service;

import com.nexra.hrms.nexra.modules.hrms.timesheet.dto.request.ProjectUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.timesheet.dto.request.TimesheetDecisionRequest;
import com.nexra.hrms.nexra.modules.hrms.timesheet.dto.request.TimesheetEntryCreateRequest;
import com.nexra.hrms.nexra.modules.hrms.timesheet.model.ProjectView;
import com.nexra.hrms.nexra.modules.hrms.timesheet.model.TimesheetEntryView;
import com.nexra.hrms.nexra.modules.hrms.timesheet.security.AuthenticatedTimesheetUser;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Defines tenant-scoped timesheet business operations for projects, entries, approvals, and summaries.
 */
public interface TimesheetService {

    ProjectView upsertProject(ProjectUpsertRequest request, AuthenticatedTimesheetUser actor);

    List<ProjectView> listProjects(String tenantCode, boolean includeInactive, AuthenticatedTimesheetUser actor);

    TimesheetEntryView createEntry(TimesheetEntryCreateRequest request, AuthenticatedTimesheetUser actor);

    List<TimesheetEntryView> listEntries(
        String tenantCode,
        String employeeId,
        LocalDate fromDate,
        LocalDate toDate,
        AuthenticatedTimesheetUser actor
    );

    TimesheetEntryView getEntry(String tenantCode, String entryId, AuthenticatedTimesheetUser actor);

    TimesheetEntryView approveEntry(String entryId, TimesheetDecisionRequest request, AuthenticatedTimesheetUser actor);

    TimesheetEntryView rejectEntry(String entryId, TimesheetDecisionRequest request, AuthenticatedTimesheetUser actor);

    Map<String, Object> summary(
        String tenantCode,
        String employeeId,
        LocalDate fromDate,
        LocalDate toDate,
        AuthenticatedTimesheetUser actor
    );
}

