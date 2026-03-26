package com.nexra.hrms.nexra.modules.auth.enums;

/**
 * Enumerates product-specific roles assignable within HRMS and CRM products.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public enum ProductRole {

    /**
     * HRMS product roles.
     */
    EMPLOYEE,
    HR_MANAGER,
    PAYROLL_ADMIN,
    DEPARTMENT_HEAD,

    /**
     * CRM product roles.
     */
    SALES_REP,
    ACCOUNT_MANAGER,
    SALES_MANAGER,
    SUPPORT_AGENT,

    /**
     * Common administrative role applicable to any product.
     */
    TENANT_ADMIN
}
