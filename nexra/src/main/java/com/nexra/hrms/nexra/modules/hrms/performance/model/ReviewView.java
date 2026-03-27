package com.nexra.hrms.nexra.modules.hrms.performance.model; import java.math.BigDecimal; public record ReviewView(String reviewId,String tenantCode,String employeeId,String reviewCycle,String status,BigDecimal managerScore,String employeeComments,String managerComments) {}

