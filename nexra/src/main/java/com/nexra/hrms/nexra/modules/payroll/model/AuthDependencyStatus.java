package com.nexra.hrms.nexra.modules.payroll.model;

public record AuthDependencyStatus(
    boolean reachable,
    String status,
    String detail
) {
}
