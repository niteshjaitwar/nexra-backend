package com.nexra.hrms.nexra.modules.auth.service.impl.security;

import com.nexra.hrms.nexra.common.audit.AuditEventRecord;
import com.nexra.hrms.nexra.common.audit.AuditEventService;
import com.nexra.hrms.nexra.modules.auth.service.security.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

/**
 * Emits structured security audit events through the application logger.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LoggingSecurityAuditService implements SecurityAuditService {

    private final AuditEventService auditEventService;

    @Override
    public void record(
        final String eventType,
        final String tenantCode,
        final String email,
        final String outcome,
        final String details
    ) {
        log.info(
            "SECURITY_AUDIT eventType={}, tenantCode={}, email={}, outcome={}, details={}",
            eventType,
            tenantCode == null ? "-" : tenantCode,
            maskEmail(email),
            outcome,
            details
        );
        auditEventService.record(
            AuditEventRecord.of(tenantCode, "AUTH", eventType, outcome)
                .withActor(email, null)
                .withDetail(details)
                .withRequestInfo(null, MDC.get("requestId"))
        );
    }

    private String maskEmail(final String email) {
        if (email == null || email.isBlank()) {
            return "-";
        }
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
