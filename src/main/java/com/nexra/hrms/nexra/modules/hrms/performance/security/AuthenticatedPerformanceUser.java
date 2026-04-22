package com.nexra.hrms.nexra.modules.hrms.performance.security; import java.util.Set; import java.util.UUID; public record AuthenticatedPerformanceUser(UUID userId,String email,String tenantCode,Set<String> roles) {}

