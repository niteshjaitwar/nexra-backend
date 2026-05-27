package com.nexra.hrms.nexra.modules.crm;

import com.nexra.hrms.nexra.common.exception.NexraForbiddenException;
import com.nexra.hrms.nexra.common.exception.NexraUnauthorizedException;
import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.support.CrmRequestContextResolver;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrmRequestContextResolverTest {

    private final CrmRequestContextResolver resolver = new CrmRequestContextResolver();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void resolveTenantCodeReturnsPrincipalTenantWhenAuthenticated() {
        CrmProperties properties = new CrmProperties();
        properties.setEnforceAuth(true);
        JwtPrincipal principal = principal("ACME", Set.of("CRM"));
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(principal, null));

        String tenantCode = resolver.resolveTenantCode(properties);

        assertThat(tenantCode).isEqualTo("ACME");
    }

    @Test
    void resolveTenantCodeReturnsDevWhenAuthDisabledAndAnonymous() {
        CrmProperties properties = new CrmProperties();
        properties.setEnforceAuth(false);

        String tenantCode = resolver.resolveTenantCode(properties);

        assertThat(tenantCode).isEqualTo("DEV");
    }

    @Test
    void resolveTenantCodeRejectsWhenProductAccessMissing() {
        CrmProperties properties = new CrmProperties();
        properties.setEnforceAuth(true);
        JwtPrincipal principal = principal("ACME", Set.of("HRMS"));
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(principal, null));

        assertThatThrownBy(() -> resolver.resolveTenantCode(properties))
            .isInstanceOf(NexraForbiddenException.class)
            .hasMessage("User does not have CRM product access.");
    }

    @Test
    void resolveAuthenticatedPrincipalRequiresAuthenticationEvenWhenAuthDisabled() {
        CrmProperties properties = new CrmProperties();
        properties.setEnforceAuth(false);

        assertThatThrownBy(() -> resolver.resolveAuthenticatedPrincipal(properties))
            .isInstanceOf(NexraUnauthorizedException.class)
            .hasMessage("CRM administration requires authentication.");
    }

    private JwtPrincipal principal(final String tenantCode, final Set<String> products) {
        return new JwtPrincipal(
            UUID.randomUUID(),
            tenantCode,
            "admin@nexra.local",
            Set.of("ROLE_USER"),
            products,
            Map.of("CRM", "TENANT_ADMIN")
        );
    }
}

