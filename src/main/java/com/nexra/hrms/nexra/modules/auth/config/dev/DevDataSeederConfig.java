package com.nexra.hrms.nexra.modules.auth.config.dev;

import com.nexra.hrms.nexra.modules.auth.entity.Tenant;
import com.nexra.hrms.nexra.modules.auth.entity.UserAccount;
import com.nexra.hrms.nexra.modules.auth.entity.UserProductAccess;
import com.nexra.hrms.nexra.modules.auth.enums.AccountType;
import com.nexra.hrms.nexra.modules.auth.enums.ProductRole;
import com.nexra.hrms.nexra.modules.auth.enums.ProductType;
import com.nexra.hrms.nexra.modules.auth.enums.UserRole;
import com.nexra.hrms.nexra.modules.auth.enums.UserStatus;
import com.nexra.hrms.nexra.modules.auth.repository.TenantRepository;
import com.nexra.hrms.nexra.modules.auth.repository.UserAccountRepository;
import com.nexra.hrms.nexra.modules.auth.repository.UserProductAccessRepository;
import com.nexra.hrms.nexra.modules.crm.entity.CrmLeadEntity;
import com.nexra.hrms.nexra.modules.crm.model.CrmLeadStatus;
import com.nexra.hrms.nexra.modules.crm.repository.CrmLeadRepository;
import com.nexra.hrms.nexra.modules.hrms.attendance.entity.AttendanceRecordEntity;
import com.nexra.hrms.nexra.modules.hrms.attendance.entity.ShiftEntity;
import com.nexra.hrms.nexra.modules.hrms.attendance.repository.AttendanceRecordRepository;
import com.nexra.hrms.nexra.modules.hrms.attendance.repository.ShiftRepository;
import com.nexra.hrms.nexra.modules.hrms.employee.entity.DepartmentEntity;
import com.nexra.hrms.nexra.modules.hrms.employee.entity.EmployeeEntity;
import com.nexra.hrms.nexra.modules.hrms.employee.entity.OrganizationProfileEntity;
import com.nexra.hrms.nexra.modules.hrms.employee.repository.DepartmentRepository;
import com.nexra.hrms.nexra.modules.hrms.employee.repository.EmployeeRepository;
import com.nexra.hrms.nexra.modules.hrms.employee.repository.OrganizationProfileRepository;
import com.nexra.hrms.nexra.modules.hrms.leave.entity.LeaveBalanceEntity;
import com.nexra.hrms.nexra.modules.hrms.leave.entity.LeaveRequestEntity;
import com.nexra.hrms.nexra.modules.hrms.leave.entity.LeaveTypeEntity;
import com.nexra.hrms.nexra.modules.hrms.leave.repository.LeaveBalanceRepository;
import com.nexra.hrms.nexra.modules.hrms.leave.repository.LeaveRequestRepository;
import com.nexra.hrms.nexra.modules.hrms.leave.repository.LeaveTypeRepository;
import com.nexra.hrms.nexra.modules.hrms.timesheet.entity.ProjectEntity;
import com.nexra.hrms.nexra.modules.hrms.timesheet.entity.TimesheetEntryEntity;
import com.nexra.hrms.nexra.modules.hrms.timesheet.repository.ProjectRepository;
import com.nexra.hrms.nexra.modules.hrms.timesheet.repository.TimesheetEntryRepository;
import com.nexra.hrms.nexra.modules.payroll.entity.PayrollEmployeeProfileEntity;
import com.nexra.hrms.nexra.modules.payroll.entity.PayrollOrganizationProfileEntity;
import com.nexra.hrms.nexra.modules.payroll.entity.PayrollSlipEntity;
import com.nexra.hrms.nexra.modules.payroll.repository.PayrollEmployeeProfileRepository;
import com.nexra.hrms.nexra.modules.payroll.repository.PayrollOrganizationProfileRepository;
import com.nexra.hrms.nexra.modules.payroll.repository.PayrollSlipRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@Profile({"dev", "e2e"})
public class DevDataSeederConfig {

    private static final String DEFAULT_TENANT_CODE = "nexra";
    private static final String DEFAULT_TENANT_NAME = "Nexra Default Tenant";
    private static final String DEV_PASSWORD = "Password@1234";
    private static final String DEV_ADMIN_EMAIL = "dev.admin@nexra.local";
    private static final int MIN_DEV_USERS = 10;

    @Bean
    public CommandLineRunner seedMinimumUsersForDev(
        final TenantRepository tenantRepository,
        final UserAccountRepository userAccountRepository,
        final UserProductAccessRepository userProductAccessRepository,
        final OrganizationProfileRepository organizationProfileRepository,
        final DepartmentRepository departmentRepository,
        final EmployeeRepository employeeRepository,
        final LeaveTypeRepository leaveTypeRepository,
        final LeaveBalanceRepository leaveBalanceRepository,
        final LeaveRequestRepository leaveRequestRepository,
        final ShiftRepository shiftRepository,
        final AttendanceRecordRepository attendanceRecordRepository,
        final ProjectRepository projectRepository,
        final TimesheetEntryRepository timesheetEntryRepository,
        final PayrollOrganizationProfileRepository payrollOrganizationProfileRepository,
        final PayrollEmployeeProfileRepository payrollEmployeeProfileRepository,
        final PayrollSlipRepository payrollSlipRepository,
        final CrmLeadRepository crmLeadRepository,
        final PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Tenant tenant = tenantRepository.findByCodeIgnoreCaseAndActiveTrue(DEFAULT_TENANT_CODE)
                .orElseGet(() -> {
                    Tenant t = new Tenant();
                    t.setCode(DEFAULT_TENANT_CODE);
                    t.setName(DEFAULT_TENANT_NAME);
                    t.setEnterprise(true);
                    t.setActive(true);
                    return tenantRepository.save(t);
                });

            UserAccount admin = upsertUser(
                userAccountRepository,
                tenant,
                passwordEncoder,
                DEV_ADMIN_EMAIL,
                "Dev",
                "Admin",
                Set.of(UserRole.ROLE_USER, UserRole.ROLE_PLATFORM_ADMIN)
            );
            upsertProductAccess(userProductAccessRepository, admin, ProductType.HRMS, ProductRole.TENANT_ADMIN, admin.getId().toString());
            upsertProductAccess(userProductAccessRepository, admin, ProductType.CRM, ProductRole.SALES_MANAGER, admin.getId().toString());

            ArrayList<UserAccount> seededUsers = new ArrayList<>();
            seededUsers.add(admin);
            for (int index = 1; index <= MIN_DEV_USERS; index++) {
                UserAccount user = upsertUser(
                    userAccountRepository,
                    tenant,
                    passwordEncoder,
                    "dev.user" + index + "@nexra.local",
                    "Dev" + index,
                    "User",
                    Set.of(UserRole.ROLE_USER)
                );
                upsertProductAccess(userProductAccessRepository, user, ProductType.HRMS, ProductRole.EMPLOYEE, admin.getId().toString());
                if (index <= 3) {
                    upsertProductAccess(userProductAccessRepository, user, ProductType.CRM, ProductRole.SALES_REP, admin.getId().toString());
                }
                seededUsers.add(user);
            }

            OrganizationProfileEntity orgProfile = upsertOrganizationProfile(organizationProfileRepository);
            DepartmentEntity engineering = upsertDepartment(departmentRepository, "ENG", "Engineering", null);
            DepartmentEntity sales = upsertDepartment(departmentRepository, "SALES", "Sales", null);
            DepartmentEntity hr = upsertDepartment(departmentRepository, "HR", "People Operations", null);

            UserAccount user1 = seededUsers.get(1);
            UserAccount user2 = seededUsers.get(2);
            UserAccount user3 = seededUsers.get(3);
            UserAccount user4 = seededUsers.get(4);

            EmployeeEntity employee1 = upsertEmployee(employeeRepository, user1, "EMP-1001", "Sales Executive", sales.getId(), new BigDecimal("82000"));
            EmployeeEntity employee2 = upsertEmployee(employeeRepository, user2, "EMP-1002", "Business Development", sales.getId(), new BigDecimal("78000"));
            EmployeeEntity employee3 = upsertEmployee(employeeRepository, user3, "EMP-1003", "HR Operations", hr.getId(), new BigDecimal("69000"));
            EmployeeEntity employee4 = upsertEmployee(employeeRepository, user4, "EMP-1004", "Software Engineer", engineering.getId(), new BigDecimal("96000"));

            LeaveTypeEntity annualLeave = upsertLeaveType(leaveTypeRepository, "ANNUAL", "Annual Leave", true, new BigDecimal("24"));
            LeaveTypeEntity sickLeave = upsertLeaveType(leaveTypeRepository, "SICK", "Sick Leave", true, new BigDecimal("12"));
            upsertLeaveBalance(leaveBalanceRepository, employee1.getId(), annualLeave.getCode(), new BigDecimal("20"), new BigDecimal("3"));
            upsertLeaveBalance(leaveBalanceRepository, employee1.getId(), sickLeave.getCode(), new BigDecimal("10"), new BigDecimal("1"));
            upsertLeaveBalance(leaveBalanceRepository, employee2.getId(), annualLeave.getCode(), new BigDecimal("18"), new BigDecimal("2"));
            upsertLeaveBalance(leaveBalanceRepository, employee3.getId(), annualLeave.getCode(), new BigDecimal("21"), new BigDecimal("1"));
            upsertLeaveRequest(leaveRequestRepository, employee1, annualLeave.getCode(), "SUBMITTED", 2);
            upsertLeaveRequest(leaveRequestRepository, employee2, sickLeave.getCode(), "APPROVED", 1);

            ShiftEntity dayShift = upsertShift(shiftRepository, "DAY", "General Day Shift");
            seedAttendance(attendanceRecordRepository, employee1, dayShift.getCode(), 6);
            seedAttendance(attendanceRecordRepository, employee2, dayShift.getCode(), 6);

            ProjectEntity crmProject = upsertProject(projectRepository, "CRM-PIPE", "CRM Pipeline Revamp", "Nexra CRM Accounts", true);
            ProjectEntity hrProject = upsertProject(projectRepository, "HR-OPS", "HR Automation", "Nexra People Ops", false);
            seedTimesheet(timesheetEntryRepository, employee1, crmProject, 5);
            seedTimesheet(timesheetEntryRepository, employee3, hrProject, 5);

            upsertPayrollOrganizationProfile(payrollOrganizationProfileRepository, orgProfile);
            upsertPayrollEmployeeProfile(payrollEmployeeProfileRepository, employee1);
            upsertPayrollEmployeeProfile(payrollEmployeeProfileRepository, employee2);
            upsertPayrollSlip(payrollSlipRepository, employee1, admin);
            upsertPayrollSlip(payrollSlipRepository, employee2, admin);

            upsertCrmLead(crmLeadRepository, admin, "Arihant Retail", "contact@arihantretail.com", "Arihant Retail Pvt Ltd", CrmLeadStatus.QUALIFIED);
            upsertCrmLead(crmLeadRepository, admin, "Bluefin Logistics", "ops@bluefinlogistics.com", "Bluefin Logistics", CrmLeadStatus.CONTACTED);
            upsertCrmLead(crmLeadRepository, admin, "Meka Systems", "partnerships@mekasystems.io", "Meka Systems", CrmLeadStatus.WON);
            upsertCrmLead(crmLeadRepository, admin, "HealthStack", "hello@healthstack.ai", "HealthStack", CrmLeadStatus.NEW);
            upsertCrmLead(crmLeadRepository, admin, "Sparrow Finance", "procurement@sparrowfinance.in", "Sparrow Finance", CrmLeadStatus.LOST);

            long tenantUserCount = userAccountRepository.countByTenant(tenant);
            log.info(
                "DevDataSeederConfig() - seedMinimumUsersForDev() - Dev records seeded successfully, tenantCode={}, tenantUserCount={}, employees={}, crmLeads={}",
                tenant.getCode(),
                tenantUserCount,
                employeeRepository.countByTenantCodeIgnoreCaseAndActiveTrue(DEFAULT_TENANT_CODE),
                crmLeadRepository.countByTenantCodeIgnoreCase(DEFAULT_TENANT_CODE)
            );
        };
    }

    private UserAccount upsertUser(
        final UserAccountRepository repository,
        final Tenant tenant,
        final PasswordEncoder passwordEncoder,
        final String email,
        final String firstName,
        final String lastName,
        final Set<UserRole> roles
    ) {
        UserAccount user = repository.findByTenantAndEmailIgnoreCase(tenant, email).orElseGet(UserAccount::new);
        user.setTenant(tenant);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(DEV_PASSWORD));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmailVerified(true);
        user.setMfaEnabled(false);
        user.setAccountType(AccountType.ENTERPRISE);
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(roles);
        return repository.save(user);
    }

    private void upsertProductAccess(
        final UserProductAccessRepository repository,
        final UserAccount user,
        final ProductType productType,
        final ProductRole productRole,
        final String grantedBy
    ) {
        UserProductAccess access = repository.findByUserAndProduct(user, productType).orElseGet(UserProductAccess::new);
        access.setUser(user);
        access.setProduct(productType);
        access.setProductRole(productRole);
        access.setGrantedAt(Instant.now());
        access.setGrantedBy(grantedBy);
        repository.save(access);
    }

    private OrganizationProfileEntity upsertOrganizationProfile(final OrganizationProfileRepository repository) {
        OrganizationProfileEntity profile = repository.findByTenantCodeIgnoreCase(DEFAULT_TENANT_CODE).orElseGet(OrganizationProfileEntity::new);
        profile.setId(profile.getId() != null ? profile.getId() : UUID.randomUUID().toString());
        profile.setTenantCode(DEFAULT_TENANT_CODE);
        profile.setOrganizationName("Nexra Technologies");
        profile.setLegalEntityName("Nexra Technologies Private Limited");
        profile.setAddressLine1("Tech Park, Sector 142");
        profile.setAddressLine2("Floor 6");
        profile.setCity("Noida");
        profile.setState("Uttar Pradesh");
        profile.setCountry("India");
        profile.setPostalCode("201305");
        profile.setCurrency("INR");
        profile.setDefaultTaxPercent(new BigDecimal("10"));
        profile.setDefaultProvidentFundPercent(new BigDecimal("12"));
        profile.setPayrollContactEmail("payroll@nexra.local");
        profile.setPayrollContactPhone("+91-9999000011");
        return repository.save(profile);
    }

    private DepartmentEntity upsertDepartment(
        final DepartmentRepository repository,
        final String code,
        final String name,
        final String managerEmployeeId
    ) {
        DepartmentEntity entity = repository.findByTenantCodeIgnoreCaseAndCodeIgnoreCase(DEFAULT_TENANT_CODE, code).orElseGet(DepartmentEntity::new);
        entity.setId(entity.getId() != null ? entity.getId() : UUID.randomUUID().toString());
        entity.setTenantCode(DEFAULT_TENANT_CODE);
        entity.setCode(code);
        entity.setName(name);
        entity.setManagerEmployeeId(managerEmployeeId);
        entity.setActive(true);
        return repository.save(entity);
    }

    private EmployeeEntity upsertEmployee(
        final EmployeeRepository repository,
        final UserAccount user,
        final String employeeCode,
        final String designation,
        final String departmentId,
        final BigDecimal monthlyBasicSalary
    ) {
        EmployeeEntity employee = repository.findByTenantCodeIgnoreCaseAndWorkEmailIgnoreCase(DEFAULT_TENANT_CODE, user.getEmail())
            .orElseGet(EmployeeEntity::new);
        employee.setId(user.getId().toString());
        employee.setTenantCode(DEFAULT_TENANT_CODE);
        employee.setEmployeeCode(employeeCode);
        employee.setFirstName(user.getFirstName());
        employee.setLastName(user.getLastName());
        employee.setWorkEmail(user.getEmail());
        employee.setDepartmentId(departmentId);
        employee.setDesignation(designation);
        employee.setStatus("ACTIVE");
        employee.setJoinDate(LocalDate.now().minusMonths(8));
        employee.setMonthlyBasicSalary(monthlyBasicSalary);
        employee.setBankName("State Bank of India");
        employee.setBankAccountMasked("XXXXXX" + employeeCode.substring(employeeCode.length() - 2) + "01");
        employee.setPanMasked("ABCDE1234X");
        employee.setUanMasked("10020030" + employeeCode.substring(employeeCode.length() - 2));
        employee.setActive(true);
        return repository.save(employee);
    }

    private LeaveTypeEntity upsertLeaveType(
        final LeaveTypeRepository repository,
        final String code,
        final String name,
        final boolean paid,
        final BigDecimal defaultQuota
    ) {
        LeaveTypeEntity leaveType = repository.findByTenantCodeIgnoreCaseAndCodeIgnoreCase(DEFAULT_TENANT_CODE, code).orElseGet(LeaveTypeEntity::new);
        leaveType.setId(leaveType.getId() != null ? leaveType.getId() : UUID.randomUUID().toString());
        leaveType.setTenantCode(DEFAULT_TENANT_CODE);
        leaveType.setCode(code);
        leaveType.setName(name);
        leaveType.setPaid(paid);
        leaveType.setDefaultAnnualQuota(defaultQuota);
        leaveType.setActive(true);
        return repository.save(leaveType);
    }

    private void upsertLeaveBalance(
        final LeaveBalanceRepository repository,
        final String employeeId,
        final String leaveTypeCode,
        final BigDecimal openingBalance,
        final BigDecimal usedBalance
    ) {
        LeaveBalanceEntity balance = repository.findByTenantCodeIgnoreCaseAndEmployeeIdAndLeaveTypeCodeIgnoreCase(DEFAULT_TENANT_CODE, employeeId, leaveTypeCode)
            .orElseGet(LeaveBalanceEntity::new);
        balance.setId(balance.getId() != null ? balance.getId() : UUID.randomUUID().toString());
        balance.setTenantCode(DEFAULT_TENANT_CODE);
        balance.setEmployeeId(employeeId);
        balance.setLeaveTypeCode(leaveTypeCode);
        balance.setOpeningBalance(openingBalance);
        balance.setAccruedBalance(new BigDecimal("2"));
        balance.setUsedBalance(usedBalance);
        balance.setAdjustedBalance(BigDecimal.ZERO);
        repository.save(balance);
    }

    private void upsertLeaveRequest(
        final LeaveRequestRepository repository,
        final EmployeeEntity employee,
        final String leaveTypeCode,
        final String status,
        final int days
    ) {
        LeaveRequestEntity existing = repository.findByTenantCodeIgnoreCaseAndEmployeeIdOrderByCreatedAtDesc(DEFAULT_TENANT_CODE, employee.getId())
            .stream()
            .filter((request) -> leaveTypeCode.equalsIgnoreCase(request.getLeaveTypeCode()) && status.equalsIgnoreCase(request.getStatus()))
            .findFirst()
            .orElseGet(LeaveRequestEntity::new);
        LocalDate startDate = LocalDate.now().plusDays(3);
        existing.setId(existing.getId() != null ? existing.getId() : UUID.randomUUID().toString());
        existing.setTenantCode(DEFAULT_TENANT_CODE);
        existing.setEmployeeId(employee.getId());
        existing.setLeaveTypeCode(leaveTypeCode);
        existing.setStartDate(startDate);
        existing.setEndDate(startDate.plusDays(days - 1L));
        existing.setTotalDays(new BigDecimal(days));
        existing.setReason("Seeded leave request for local functional testing");
        existing.setStatus(status);
        existing.setApproverUserId(null);
        existing.setApproverEmail("dev.admin@nexra.local");
        existing.setDecisionComment("Seeded by dev profile");
        repository.save(existing);
    }

    private ShiftEntity upsertShift(final ShiftRepository repository, final String code, final String name) {
        ShiftEntity shift = repository.findByTenantCodeIgnoreCaseAndCodeIgnoreCase(DEFAULT_TENANT_CODE, code).orElseGet(ShiftEntity::new);
        shift.setId(shift.getId() != null ? shift.getId() : UUID.randomUUID().toString());
        shift.setTenantCode(DEFAULT_TENANT_CODE);
        shift.setCode(code);
        shift.setName(name);
        shift.setStartTime("09:30");
        shift.setEndTime("18:30");
        shift.setGraceMinutes(15);
        shift.setActive(true);
        return repository.save(shift);
    }

    private void seedAttendance(
        final AttendanceRecordRepository repository,
        final EmployeeEntity employee,
        final String shiftCode,
        final int days
    ) {
        for (int offset = 1; offset <= days; offset++) {
            LocalDate workDate = LocalDate.now().minusDays(offset);
            AttendanceRecordEntity record = repository.findByTenantCodeIgnoreCaseAndEmployeeIdAndWorkDate(DEFAULT_TENANT_CODE, employee.getId(), workDate)
                .orElseGet(AttendanceRecordEntity::new);
            Instant checkIn = workDate.atTime(9, 35).toInstant(ZoneOffset.UTC);
            Instant checkOut = workDate.atTime(18, 15).toInstant(ZoneOffset.UTC);
            record.setId(record.getId() != null ? record.getId() : UUID.randomUUID().toString());
            record.setTenantCode(DEFAULT_TENANT_CODE);
            record.setEmployeeId(employee.getId());
            record.setWorkDate(workDate);
            record.setShiftCode(shiftCode);
            record.setCheckInAt(checkIn);
            record.setCheckOutAt(checkOut);
            record.setTotalHours(new BigDecimal("8.67"));
            record.setStatus(offset % 4 == 0 ? "LATE" : "PRESENT");
            record.setNotes("Seeded attendance record");
            repository.save(record);
        }
    }

    private ProjectEntity upsertProject(
        final ProjectRepository repository,
        final String projectCode,
        final String projectName,
        final String clientName,
        final boolean billable
    ) {
        ProjectEntity project = repository.findByTenantCodeIgnoreCaseAndProjectCodeIgnoreCase(DEFAULT_TENANT_CODE, projectCode).orElseGet(ProjectEntity::new);
        project.setId(project.getId() != null ? project.getId() : UUID.randomUUID().toString());
        project.setTenantCode(DEFAULT_TENANT_CODE);
        project.setProjectCode(projectCode);
        project.setProjectName(projectName);
        project.setClientName(clientName);
        project.setBillable(billable);
        project.setActive(true);
        return repository.save(project);
    }

    private void seedTimesheet(
        final TimesheetEntryRepository repository,
        final EmployeeEntity employee,
        final ProjectEntity project,
        final int days
    ) {
        LocalDate fromDate = LocalDate.now().minusDays(days + 1L);
        LocalDate toDate = LocalDate.now();
        for (int offset = 1; offset <= days; offset++) {
            LocalDate workDate = LocalDate.now().minusDays(offset);
            boolean exists = repository.findByTenantCodeIgnoreCaseAndEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(
                DEFAULT_TENANT_CODE,
                employee.getId(),
                fromDate,
                toDate
            ).stream().anyMatch((row) -> row.getWorkDate().equals(workDate) && project.getProjectCode().equalsIgnoreCase(row.getProjectCode()));
            if (exists) {
                continue;
            }
            TimesheetEntryEntity entry = new TimesheetEntryEntity();
            entry.setId(UUID.randomUUID().toString());
            entry.setTenantCode(DEFAULT_TENANT_CODE);
            entry.setEmployeeId(employee.getId());
            entry.setWorkDate(workDate);
            entry.setProjectCode(project.getProjectCode());
            entry.setTaskName("Seeded delivery workflow task");
            entry.setHours(new BigDecimal("7.50"));
            entry.setBillable(project.isBillable());
            entry.setStatus(offset % 2 == 0 ? "SUBMITTED" : "APPROVED");
            entry.setApproverEmail("dev.admin@nexra.local");
            entry.setApproverUserId(null);
            entry.setApprovalComment("Seeded by dev profile");
            entry.setNotes("Deterministic seeded timesheet entry");
            repository.save(entry);
        }
    }

    private void upsertPayrollOrganizationProfile(
        final PayrollOrganizationProfileRepository repository,
        final OrganizationProfileEntity profile
    ) {
        PayrollOrganizationProfileEntity payrollProfile = repository.findByTenantCodeIgnoreCase(DEFAULT_TENANT_CODE).orElseGet(PayrollOrganizationProfileEntity::new);
        payrollProfile.setId(payrollProfile.getId() != null ? payrollProfile.getId() : UUID.randomUUID().toString());
        payrollProfile.setTenantCode(DEFAULT_TENANT_CODE);
        payrollProfile.setOrganizationName(profile.getOrganizationName());
        payrollProfile.setLegalEntityName(profile.getLegalEntityName());
        payrollProfile.setAddressLine1(profile.getAddressLine1());
        payrollProfile.setAddressLine2(profile.getAddressLine2());
        payrollProfile.setCity(profile.getCity());
        payrollProfile.setState(profile.getState());
        payrollProfile.setCountry(profile.getCountry());
        payrollProfile.setPostalCode(profile.getPostalCode());
        payrollProfile.setCurrency(profile.getCurrency());
        payrollProfile.setDefaultTaxPercent(new BigDecimal("10"));
        payrollProfile.setDefaultProvidentFundPercent(new BigDecimal("12"));
        payrollProfile.setPayrollContactEmail(profile.getPayrollContactEmail());
        payrollProfile.setPayrollContactPhone(profile.getPayrollContactPhone());
        repository.save(payrollProfile);
    }

    private void upsertPayrollEmployeeProfile(
        final PayrollEmployeeProfileRepository repository,
        final EmployeeEntity employee
    ) {
        PayrollEmployeeProfileEntity profile = repository.findByTenantCodeIgnoreCaseAndEmployeeId(DEFAULT_TENANT_CODE, employee.getId())
            .orElseGet(PayrollEmployeeProfileEntity::new);
        profile.setId(profile.getId() != null ? profile.getId() : UUID.randomUUID().toString());
        profile.setTenantCode(DEFAULT_TENANT_CODE);
        profile.setEmployeeId(employee.getId());
        profile.setEmployeeCode(employee.getEmployeeCode());
        profile.setEmployeeName(employee.getFirstName() + " " + employee.getLastName());
        profile.setDepartment(employee.getDepartmentId() == null ? "General" : employee.getDepartmentId());
        profile.setDesignation(employee.getDesignation());
        profile.setMonthlyBasicSalary(employee.getMonthlyBasicSalary());
        profile.setBankName(employee.getBankName());
        profile.setBankAccountMasked(employee.getBankAccountMasked());
        profile.setPanMasked(employee.getPanMasked());
        profile.setUanMasked(employee.getUanMasked());
        profile.setEmail(employee.getWorkEmail());
        repository.save(profile);
    }

    private void upsertPayrollSlip(
        final PayrollSlipRepository repository,
        final EmployeeEntity employee,
        final UserAccount admin
    ) {
        String payPeriod = LocalDate.now().minusMonths(1).toString().substring(0, 7);
        boolean exists = repository.findByTenantCodeIgnoreCaseOrderByGeneratedAtDesc(DEFAULT_TENANT_CODE).stream()
            .anyMatch((slip) -> slip.getEmployeeId().equals(employee.getId()) && payPeriod.equalsIgnoreCase(slip.getPayPeriod()));
        if (exists) {
            return;
        }
        BigDecimal basic = employee.getMonthlyBasicSalary();
        BigDecimal taxPercent = new BigDecimal("10");
        BigDecimal pfPercent = new BigDecimal("12");
        BigDecimal taxAmount = basic.multiply(taxPercent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal pfAmount = basic.multiply(pfPercent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        PayrollSlipEntity slip = new PayrollSlipEntity();
        slip.setSlipId(UUID.randomUUID().toString());
        slip.setTenantCode(DEFAULT_TENANT_CODE);
        slip.setEmployeeId(employee.getId());
        slip.setEmployeeCode(employee.getEmployeeCode());
        slip.setEmployeeName(employee.getFirstName() + " " + employee.getLastName());
        slip.setDepartment(employee.getDepartmentId() == null ? "General" : employee.getDepartmentId());
        slip.setDesignation(employee.getDesignation());
        slip.setPayPeriod(payPeriod);
        slip.setCurrency("INR");
        slip.setOrganizationProfileJson("{\"organizationName\":\"Nexra Technologies\"}");
        slip.setEmployeeProfileJson("{\"employeeCode\":\"" + employee.getEmployeeCode() + "\"}");
        slip.setAllowancesJson("[{\"label\":\"HRA\",\"amount\":12000}]");
        slip.setDeductionsJson("[{\"label\":\"Tax\",\"amount\":" + taxAmount + "}]");
        slip.setAuthDependencyStatusJson("{\"status\":\"UP\"}");
        slip.setBasicSalary(basic);
        slip.setTaxPercent(taxPercent);
        slip.setProvidentFundPercent(pfPercent);
        slip.setTaxAmount(taxAmount);
        slip.setProvidentFundAmount(pfAmount);
        slip.setGrossEarnings(basic.add(new BigDecimal("12000")));
        slip.setTotalDeductions(taxAmount.add(pfAmount));
        slip.setNetPay(slip.getGrossEarnings().subtract(slip.getTotalDeductions()));
        slip.setGeneratedAt(Instant.now());
        slip.setGeneratedByEmail(admin.getEmail());
        slip.setGeneratedByUserId(admin.getId().toString());
        repository.save(slip);
    }

    private void upsertCrmLead(
        final CrmLeadRepository repository,
        final UserAccount owner,
        final String fullName,
        final String email,
        final String company,
        final CrmLeadStatus status
    ) {
        if (repository.existsByTenantCodeIgnoreCaseAndEmailIgnoreCase(DEFAULT_TENANT_CODE, email)) {
            return;
        }
        Instant now = Instant.now();
        CrmLeadEntity entity = new CrmLeadEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantCode(DEFAULT_TENANT_CODE);
        entity.setFullName(fullName);
        entity.setEmail(email);
        entity.setPhone("+91-99990000" + Math.abs(email.hashCode() % 90 + 10));
        entity.setCompany(company);
        entity.setSource("Website");
        entity.setOwnerUserId(owner.getId().toString());
        entity.setNotes("Seeded CRM lead for local testing.");
        entity.setStatus(status);
        entity.setDomainCreatedAt(now);
        entity.setDomainUpdatedAt(now);
        repository.save(entity);
    }
}
