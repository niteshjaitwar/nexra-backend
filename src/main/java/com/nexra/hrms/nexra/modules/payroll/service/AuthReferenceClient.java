package com.nexra.hrms.nexra.modules.payroll.service;

import com.nexra.hrms.nexra.modules.payroll.model.AuthDependencyStatus;

/**
 * Defines outbound calls to auth service dependencies used by payroll.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public interface AuthReferenceClient {

    /**
     * Fetches auth dependency health status.
     *
     * @return auth dependency status
     */
    AuthDependencyStatus getAuthHealth();
}
