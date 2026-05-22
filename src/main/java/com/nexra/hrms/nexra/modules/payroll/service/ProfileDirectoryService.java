package com.nexra.hrms.nexra.modules.payroll.service;

import com.nexra.hrms.nexra.modules.payroll.dto.EmployeeProfileUpsertRequest;
import com.nexra.hrms.nexra.modules.payroll.dto.OrganizationProfileUpsertRequest;
import com.nexra.hrms.nexra.modules.payroll.model.EmployeeProfile;
import com.nexra.hrms.nexra.modules.payroll.model.OrganizationProfile;
import com.nexra.hrms.nexra.modules.payroll.security.AuthenticatedPayrollUser;
import java.util.List;

/**
 * Defines payroll profile directory operations for organization and employee profiles.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public interface ProfileDirectoryService {

    OrganizationProfile upsertOrganizationProfile(OrganizationProfileUpsertRequest request, AuthenticatedPayrollUser actor);

    OrganizationProfile getOrganizationProfile(String tenantCode, AuthenticatedPayrollUser actor);

    EmployeeProfile upsertEmployeeProfile(EmployeeProfileUpsertRequest request, AuthenticatedPayrollUser actor);

    EmployeeProfile getEmployeeProfile(String tenantCode, String employeeId, AuthenticatedPayrollUser actor);

    List<EmployeeProfile> listEmployeeProfiles(String tenantCode, AuthenticatedPayrollUser actor);

    OrganizationProfile updateOrganizationBrandingLogoPath(String tenantCode, String logoPath, AuthenticatedPayrollUser actor);

    OrganizationProfile getOrganizationProfileInternal(String tenantCode);

    EmployeeProfile getEmployeeProfileInternal(String tenantCode, String employeeId);
}
