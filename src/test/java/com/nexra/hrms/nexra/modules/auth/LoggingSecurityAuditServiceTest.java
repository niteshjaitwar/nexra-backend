package com.nexra.hrms.nexra.modules.auth;

import com.nexra.hrms.nexra.common.audit.AuditEventRecord;
import com.nexra.hrms.nexra.common.audit.AuditEventService;
import com.nexra.hrms.nexra.modules.auth.service.impl.security.LoggingSecurityAuditService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LoggingSecurityAuditServiceTest {

    @Test
    void shouldPersistSecurityAuditEventWithRequestId() {
        AuditEventService auditEventService = mock(AuditEventService.class);
        LoggingSecurityAuditService service = new LoggingSecurityAuditService(auditEventService);
        MDC.put("requestId", "req-audit-001");
        try {
            service.record("LOGIN", "acme", "user@acme.com", "SUCCESS", "Interactive login completed.");
        } finally {
            MDC.remove("requestId");
        }

        ArgumentCaptor<AuditEventRecord> captor = ArgumentCaptor.forClass(AuditEventRecord.class);
        verify(auditEventService).record(captor.capture());
        AuditEventRecord event = captor.getValue();
        assertThat(event.tenantCode()).isEqualTo("acme");
        assertThat(event.module()).isEqualTo("AUTH");
        assertThat(event.action()).isEqualTo("LOGIN");
        assertThat(event.outcome()).isEqualTo("SUCCESS");
        assertThat(event.actorEmail()).isEqualTo("user@acme.com");
        assertThat(event.detail()).isEqualTo("Interactive login completed.");
        assertThat(event.requestId()).isEqualTo("req-audit-001");
    }
}
