package com.nexra.hrms.nexra.modules.hrms.employee.service.impl;

import com.nexra.hrms.nexra.modules.hrms.employee.dto.request.DepartmentUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.employee.dto.request.EmployeeUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.employee.dto.request.OrganizationProfileUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.employee.entity.DepartmentEntity;
import com.nexra.hrms.nexra.modules.hrms.employee.entity.EmployeeEntity;
import com.nexra.hrms.nexra.modules.hrms.employee.entity.OrganizationProfileEntity;
import com.nexra.hrms.nexra.modules.hrms.employee.exception.EmployeeCoreBusinessException;
import com.nexra.hrms.nexra.modules.hrms.employee.exception.EmployeeCoreResourceNotFoundException;
import com.nexra.hrms.nexra.modules.hrms.employee.model.Department;
import com.nexra.hrms.nexra.modules.hrms.employee.model.Employee;
import com.nexra.hrms.nexra.modules.hrms.employee.model.OrganizationProfile;
import com.nexra.hrms.nexra.modules.hrms.employee.repository.DepartmentRepository;
import com.nexra.hrms.nexra.modules.hrms.employee.repository.EmployeeRepository;
import com.nexra.hrms.nexra.modules.hrms.employee.repository.OrganizationProfileRepository;
import com.nexra.hrms.nexra.modules.hrms.employee.security.AuthenticatedEmployeeCoreUser;
import com.nexra.hrms.nexra.common.api.PageResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.nexra.hrms.nexra.modules.hrms.employee.service.EmployeeCoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of tenant-scoped employee-core master data operations.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeCoreServiceImpl implements EmployeeCoreService {

    private final OrganizationProfileRepository organizationProfileRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public OrganizationProfile upsertOrganizationProfile(
        final OrganizationProfileUpsertRequest request,
        final AuthenticatedEmployeeCoreUser actor
    ) {
        verifyTenant(actor, request.tenantCode());
        String tenant = normTenant(request.tenantCode());
        log.info("EmployeeCoreServiceImpl - upsertOrganizationProfile - tenantCode={}, actor={}", tenant, actor.email());

        OrganizationProfileEntity entity = organizationProfileRepository.findByTenantCodeIgnoreCase(tenant)
            .orElseGet(OrganizationProfileEntity::new);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID().toString());
            entity.setCreatedBy(actor.email());
        }

        entity.setTenantCode(tenant);
        entity.setOrganizationName(trim(request.organizationName()));
        entity.setLegalEntityName(trim(request.legalEntityName()));
        entity.setAddressLine1(trim(request.addressLine1()));
        entity.setAddressLine2(blankToNull(request.addressLine2()));
        entity.setCity(trim(request.city()));
        entity.setState(trim(request.state()));
        entity.setCountry(trim(request.country()));
        entity.setPostalCode(trim(request.postalCode()));
        entity.setCurrency(trim(request.currency()).toUpperCase());
        entity.setDefaultTaxPercent(amountOrZero(request.defaultTaxPercent()));
        entity.setDefaultProvidentFundPercent(amountOrZero(request.defaultProvidentFundPercent()));
        entity.setPayrollContactEmail(blankToNullLower(request.payrollContactEmail()));
        entity.setPayrollContactPhone(blankToNull(request.payrollContactPhone()));
        entity.setUpdatedBy(actor.email());

        return toModel(organizationProfileRepository.save(entity));
    }

    @Override
    public OrganizationProfile getOrganizationProfile(final String tenantCode, final AuthenticatedEmployeeCoreUser actor) {
        verifyTenant(actor, tenantCode);
        log.debug("EmployeeCoreServiceImpl - getOrganizationProfile - tenantCode={}, actor={}", tenantCode, actor.email());
        return toModel(organizationProfileRepository.findByTenantCodeIgnoreCase(normTenant(tenantCode))
            .orElseThrow(() -> new EmployeeCoreResourceNotFoundException(
                "Organization profile not found for tenant: " + tenantCode
            )));
    }

    @Override
    @Transactional
    public Department upsertDepartment(final DepartmentUpsertRequest request, final AuthenticatedEmployeeCoreUser actor) {
        verifyTenant(actor, request.tenantCode());
        String tenant = normTenant(request.tenantCode());
        String departmentId = blankToNull(request.departmentId());
        log.info(
            "EmployeeCoreServiceImpl - upsertDepartment - tenantCode={}, departmentId={}, code={}, actor={}",
            tenant,
            departmentId,
            request.code(),
            actor.email()
        );

        if (request.managerEmployeeId() != null && !request.managerEmployeeId().isBlank()) {
            employeeRepository.findByIdAndTenantCodeIgnoreCase(request.managerEmployeeId(), tenant)
                .orElseThrow(() -> new EmployeeCoreResourceNotFoundException(
                    "Manager employee not found for tenant: " + request.managerEmployeeId()
                ));
        }

        String normalizedCode = trim(request.code()).toUpperCase();
        DepartmentEntity entity;
        if (departmentId == null) {
            entity = new DepartmentEntity();
            entity.setId(UUID.randomUUID().toString());
            entity.setCreatedBy(actor.email());
        } else {
            entity = departmentRepository.findByIdAndTenantCodeIgnoreCase(departmentId, tenant)
                .orElseThrow(() -> new EmployeeCoreResourceNotFoundException("Department not found: " + departmentId));
        }

        departmentRepository.findByTenantCodeIgnoreCaseAndCodeIgnoreCase(tenant, normalizedCode)
            .filter(existing -> !existing.getId().equals(entity.getId()))
            .ifPresent(existing -> {
                throw new EmployeeCoreBusinessException("Department code already exists for tenant: " + request.code());
            });

        entity.setTenantCode(tenant);
        entity.setCode(normalizedCode);
        entity.setName(trim(request.name()));
        entity.setManagerEmployeeId(blankToNull(request.managerEmployeeId()));
        entity.setActive(request.active() == null || request.active());
        entity.setUpdatedBy(actor.email());

        return toModel(departmentRepository.save(entity));
    }

    @Override
    public List<Department> listDepartments(
        final String tenantCode,
        final boolean includeInactive,
        final AuthenticatedEmployeeCoreUser actor
    ) {
        verifyTenant(actor, tenantCode);
        log.debug(
            "EmployeeCoreServiceImpl - listDepartments - tenantCode={}, includeInactive={}, actor={}",
            tenantCode,
            includeInactive,
            actor.email()
        );
        return departmentRepository.findByTenantCodeIgnoreCaseOrderByCodeAsc(normTenant(tenantCode)).stream()
            .filter(dept -> includeInactive || dept.isActive())
            .map(this::toModel)
            .toList();
    }

    @Override
    public Department getDepartment(
        final String tenantCode,
        final String departmentId,
        final AuthenticatedEmployeeCoreUser actor
    ) {
        verifyTenant(actor, tenantCode);
        log.debug(
            "EmployeeCoreServiceImpl - getDepartment - tenantCode={}, departmentId={}, actor={}",
            tenantCode,
            departmentId,
            actor.email()
        );
        return toModel(departmentRepository.findByIdAndTenantCodeIgnoreCase(departmentId, normTenant(tenantCode))
            .orElseThrow(() -> new EmployeeCoreResourceNotFoundException("Department not found: " + departmentId)));
    }

    @Override
    @Transactional
    public Employee upsertEmployee(final EmployeeUpsertRequest request, final AuthenticatedEmployeeCoreUser actor) {
        verifyTenant(actor, request.tenantCode());
        String tenant = normTenant(request.tenantCode());
        log.info(
            "EmployeeCoreServiceImpl - upsertEmployee - tenantCode={}, employeeId={}, employeeCode={}, actor={}",
            tenant,
            request.employeeId(),
            request.employeeCode(),
            actor.email()
        );
        String departmentId = blankToNull(request.departmentId());
        if (departmentId != null) {
            departmentRepository.findByIdAndTenantCodeIgnoreCase(departmentId, tenant)
                .orElseThrow(() -> new EmployeeCoreResourceNotFoundException(
                    "Department not found for tenant: " + departmentId
                ));
        }

        String employeeId = blankToNull(request.employeeId());
        String normalizedEmpCode = trim(request.employeeCode()).toUpperCase();
        String normalizedEmail = trim(request.workEmail()).toLowerCase();

        EmployeeEntity entity;
        if (employeeId == null) {
            entity = new EmployeeEntity();
            entity.setId(UUID.randomUUID().toString());
            entity.setCreatedBy(actor.email());
        } else {
            entity = employeeRepository.findByIdAndTenantCodeIgnoreCase(employeeId, tenant)
                .orElseThrow(() -> new EmployeeCoreResourceNotFoundException("Employee not found: " + employeeId));
        }

        employeeRepository.findByTenantCodeIgnoreCaseAndEmployeeCodeIgnoreCase(tenant, normalizedEmpCode)
            .filter(existing -> !existing.getId().equals(entity.getId()))
            .ifPresent(existing -> {
                throw new EmployeeCoreBusinessException("Employee code already exists for tenant: " + request.employeeCode());
            });

        employeeRepository.findByTenantCodeIgnoreCaseAndWorkEmailIgnoreCase(tenant, normalizedEmail)
            .filter(existing -> !existing.getId().equals(entity.getId()))
            .ifPresent(existing -> {
                throw new EmployeeCoreBusinessException("Work email already exists for tenant: " + request.workEmail());
            });

        entity.setTenantCode(tenant);
        entity.setEmployeeCode(normalizedEmpCode);
        entity.setFirstName(trim(request.firstName()));
        entity.setLastName(trim(request.lastName()));
        entity.setWorkEmail(normalizedEmail);
        entity.setDepartmentId(departmentId);
        entity.setDesignation(trim(request.designation()));
        entity.setStatus(trim(request.status()).toUpperCase());
        entity.setJoinDate(request.joinDate());
        entity.setMonthlyBasicSalary(request.monthlyBasicSalary().setScale(2, RoundingMode.HALF_UP));
        entity.setBankName(blankToNull(request.bankName()));
        entity.setBankAccountMasked(blankToNull(request.bankAccountMasked()));
        entity.setPanMasked(blankToNull(request.panMasked()));
        entity.setUanMasked(blankToNull(request.uanMasked()));
        entity.setActive(request.active() == null || request.active());
        entity.setUserAccountId(blankToNull(request.userAccountId()));
        entity.setUpdatedBy(actor.email());

        return toModel(employeeRepository.save(entity));
    }

    @Override
    public PageResponse<Employee> listEmployees(
        final String tenantCode,
        final String departmentId,
        final boolean includeInactive,
        final AuthenticatedEmployeeCoreUser actor,
        final org.springframework.data.domain.Pageable pageable
    ) {
        verifyTenant(actor, tenantCode);
        String tenant = normTenant(tenantCode);
        log.debug(
            "EmployeeCoreServiceImpl - listEmployees(paginated) - tenantCode={}, departmentId={}, includeInactive={}, page={}, size={}, actor={}",
            tenant, departmentId, includeInactive, pageable.getPageNumber(), pageable.getPageSize(), actor.email()
        );
        if (!canManageEmployeeData(actor)) {
            List<EmployeeEntity> self = findActorEmployee(tenant, actor)
                .stream()
                .filter(EmployeeEntity::isActive)
                .toList();
            List<Employee> items = pageable.getPageNumber() == 0 ? self.stream().map(this::toModel).toList() : List.of();
            return new PageResponse<>(
                items,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                self.size(),
                self.isEmpty() ? 0 : 1,
                false,
                pageable.getPageNumber() > 0 && !self.isEmpty()
            );
        }
        String departmentFilter = blankToNull(departmentId);
        org.springframework.data.domain.Page<EmployeeEntity> page;
        if (departmentFilter == null) {
            page = includeInactive
                ? employeeRepository.findByTenantCodeIgnoreCase(tenant, pageable)
                : employeeRepository.findByTenantCodeIgnoreCaseAndActiveTrue(tenant, pageable);
        } else {
            page = includeInactive
                ? employeeRepository.findByTenantCodeIgnoreCaseAndDepartmentId(tenant, departmentFilter, pageable)
                : employeeRepository.findByTenantCodeIgnoreCaseAndDepartmentIdAndActiveTrue(tenant, departmentFilter, pageable);
        }
        return com.nexra.hrms.nexra.common.api.PageResponse.map(
            com.nexra.hrms.nexra.common.api.PageResponse.from(page),
            this::toModel
        );
    }

    @Override
    public Employee getEmployee(final String tenantCode, final String employeeId, final AuthenticatedEmployeeCoreUser actor) {
        verifyTenant(actor, tenantCode);
        log.debug(
            "EmployeeCoreServiceImpl - getEmployee - tenantCode={}, employeeId={}, actor={}",
            tenantCode,
            employeeId,
            actor.email()
        );
        EmployeeEntity entity = employeeRepository.findByIdAndTenantCodeIgnoreCase(employeeId, normTenant(tenantCode))
            .orElseThrow(() -> new EmployeeCoreResourceNotFoundException("Employee not found: " + employeeId));
        if (!canManageEmployeeData(actor) && !isActorEmployee(actor, entity)) {
            throw new EmployeeCoreBusinessException("User cannot access another employee profile");
        }
        return toModel(entity);
    }

    @Override
    public Map<String, Object> summary(final String tenantCode, final AuthenticatedEmployeeCoreUser actor) {
        verifyTenant(actor, tenantCode);
        String tenant = normTenant(tenantCode);
        log.debug("EmployeeCoreServiceImpl - summary - tenantCode={}, actor={}", tenant, actor.email());
        if (!canManageEmployeeData(actor)) {
            boolean hasEmployeeProfile = findActorEmployee(tenant, actor)
                .filter(EmployeeEntity::isActive)
                .isPresent();
            return Map.of(
                "tenantCode", tenant,
                "organizationProfileConfigured", organizationProfileRepository.findByTenantCodeIgnoreCase(tenant).isPresent(),
                "activeDepartments", 0,
                "activeEmployees", hasEmployeeProfile ? 1 : 0
            );
        }
        return Map.of(
            "tenantCode", tenant,
            "organizationProfileConfigured", organizationProfileRepository.findByTenantCodeIgnoreCase(tenant).isPresent(),
            "activeDepartments", departmentRepository.countByTenantCodeIgnoreCaseAndActiveTrue(tenant),
            "activeEmployees", employeeRepository.countByTenantCodeIgnoreCaseAndActiveTrue(tenant)
        );
    }

    private OrganizationProfile toModel(final OrganizationProfileEntity entity) {
        return new OrganizationProfile(
            entity.getTenantCode(),
            entity.getOrganizationName(),
            entity.getLegalEntityName(),
            entity.getAddressLine1(),
            entity.getAddressLine2(),
            entity.getCity(),
            entity.getState(),
            entity.getCountry(),
            entity.getPostalCode(),
            entity.getCurrency(),
            entity.getDefaultTaxPercent(),
            entity.getDefaultProvidentFundPercent(),
            entity.getPayrollContactEmail(),
            entity.getPayrollContactPhone(),
            entity.getUpdatedAt(),
            entity.getUpdatedBy()
        );
    }

    private Department toModel(final DepartmentEntity entity) {
        return new Department(
            entity.getId(),
            entity.getTenantCode(),
            entity.getCode(),
            entity.getName(),
            entity.getManagerEmployeeId(),
            entity.isActive(),
            entity.getUpdatedAt(),
            entity.getUpdatedBy()
        );
    }

    private Employee toModel(final EmployeeEntity entity) {
        return new Employee(
            entity.getId(),
            entity.getTenantCode(),
            entity.getEmployeeCode(),
            entity.getFirstName(),
            entity.getLastName(),
            (entity.getFirstName() + " " + entity.getLastName()).trim(),
            entity.getWorkEmail(),
            entity.getDepartmentId(),
            entity.getDesignation(),
            entity.getStatus(),
            entity.getJoinDate(),
            entity.getMonthlyBasicSalary(),
            entity.getBankName(),
            entity.getBankAccountMasked(),
            entity.getPanMasked(),
            entity.getUanMasked(),
            entity.isActive(),
            entity.getUpdatedAt(),
            entity.getUpdatedBy(),
            entity.getUserAccountId()
        );
    }

    private void verifyTenant(final AuthenticatedEmployeeCoreUser actor, final String tenantCode) {
        if (!actor.tenantCode().equalsIgnoreCase(tenantCode)) {
            throw new EmployeeCoreBusinessException("Token tenant does not match requested tenant");
        }
    }

    private boolean canManageEmployeeData(final AuthenticatedEmployeeCoreUser actor) {
        return hasRole(actor, "PLATFORM_ADMIN") || hasRole(actor, "TENANT_ADMIN") || hasRole(actor, "HR_ADMIN");
    }

    private boolean hasRole(final AuthenticatedEmployeeCoreUser actor, final String role) {
        return actor.roles().contains(role) || actor.roles().contains("ROLE_" + role);
    }

    private Optional<EmployeeEntity> findActorEmployee(
        final String tenant,
        final AuthenticatedEmployeeCoreUser actor
    ) {
        final String userId = actor.userId().toString();
        return employeeRepository.findByTenantCodeIgnoreCaseAndUserAccountId(tenant, userId)
            .or(() -> employeeRepository.findByIdAndTenantCodeIgnoreCase(userId, tenant))
            .or(() -> employeeRepository.findByTenantCodeIgnoreCaseAndWorkEmailIgnoreCase(tenant, actor.email()));
    }

    private boolean isActorEmployee(final AuthenticatedEmployeeCoreUser actor, final EmployeeEntity entity) {
        final String userId = actor.userId().toString();
        return userId.equals(entity.getUserAccountId())
            || userId.equals(entity.getId())
            || actor.email().equalsIgnoreCase(entity.getWorkEmail());
    }

    private String normTenant(final String tenantCode) {
        return trim(tenantCode).toUpperCase();
    }

    private String trim(final String value) {
        return value == null ? null : value.trim();
    }

    private String blankToNull(final String value) {
        String trimmed = trim(value);
        return trimmed == null || trimmed.isBlank() ? null : trimmed;
    }

    private String blankToNullLower(final String value) {
        String trimmed = blankToNull(value);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    private BigDecimal amountOrZero(final BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }
}
