package com.nexra.hrms.nexra.modules.hrms.performance.model; import java.time.LocalDate; public record GoalView(String goalId,String tenantCode,String employeeId,String title,String description,LocalDate targetDate,String status) {}

