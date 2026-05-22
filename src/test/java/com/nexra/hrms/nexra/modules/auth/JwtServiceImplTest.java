package com.nexra.hrms.nexra.modules.auth;

import com.nexra.hrms.nexra.modules.auth.config.AuthProperties;
import com.nexra.hrms.nexra.modules.auth.entity.Tenant;
import com.nexra.hrms.nexra.modules.auth.entity.UserAccount;
import com.nexra.hrms.nexra.modules.auth.entity.UserProductAccess;
import com.nexra.hrms.nexra.modules.auth.enums.ProductRole;
import com.nexra.hrms.nexra.modules.auth.enums.ProductType;
import com.nexra.hrms.nexra.modules.auth.enums.UserRole;
import com.nexra.hrms.nexra.modules.auth.repository.UserProductAccessRepository;
import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import com.nexra.hrms.nexra.modules.auth.security.JwtServiceImpl;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for JWT signing and parsing without Spring context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtServiceImpl")
class JwtServiceImplTest {

    private static final String SECRET = "test-jwt-secret-test-jwt-secret-test-jwt";

    @Mock
    private AuthProperties authProperties;

    @Mock
    private AuthProperties.Jwt jwtProps;

    @Mock
    private UserProductAccessRepository userProductAccessRepository;

    private JwtServiceImpl jwtService;

    @BeforeEach
    void init() {
        when(authProperties.getJwt()).thenReturn(jwtProps);
        when(jwtProps.getSecret()).thenReturn(SECRET);
        when(jwtProps.getAccessTokenMinutes()).thenReturn(30);
        when(userProductAccessRepository.findByUser(any(UserAccount.class))).thenReturn(List.of());
        jwtService = new JwtServiceImpl(authProperties, userProductAccessRepository);
    }

    @Test
    @DisplayName("generateAccessToken and parsePrincipal round-trip preserves uid, tenant, roles")
    void shouldRoundTripToken() {
        UUID userId = UUID.randomUUID();
        Tenant tenant = new Tenant();
        tenant.setCode("acme");

        UserAccount user = mock(UserAccount.class);
        when(user.getId()).thenReturn(userId);
        when(user.getTenant()).thenReturn(tenant);
        when(user.getEmail()).thenReturn("user@acme.com");
        when(user.getRoles()).thenReturn(Set.of(UserRole.ROLE_USER, UserRole.ROLE_TENANT_ADMIN));

        String token = jwtService.generateAccessToken(user);
        JwtPrincipal principal = jwtService.parsePrincipal(token);

        assertThat(principal.userId()).isEqualTo(userId);
        assertThat(principal.tenantCode()).isEqualTo("acme");
        assertThat(principal.email()).isEqualTo("user@acme.com");
        assertThat(principal.roles()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_TENANT_ADMIN");
        assertThat(principal.products()).isEmpty();
        assertThat(principal.productRoles()).isEmpty();
    }

    @Test
    @DisplayName("generateAccessToken includes product access claims")
    void shouldIncludeProductClaims() {
        UUID userId = UUID.randomUUID();
        Tenant tenant = new Tenant();
        tenant.setCode("acme");

        UserAccount user = mock(UserAccount.class);
        when(user.getId()).thenReturn(userId);
        when(user.getTenant()).thenReturn(tenant);
        when(user.getEmail()).thenReturn("admin@acme.com");
        when(user.getRoles()).thenReturn(Set.of(UserRole.ROLE_TENANT_ADMIN));

        UserProductAccess crm = new UserProductAccess();
        crm.setProduct(ProductType.CRM);
        crm.setProductRole(ProductRole.SALES_MANAGER);
        UserProductAccess hrms = new UserProductAccess();
        hrms.setProduct(ProductType.HRMS);
        hrms.setProductRole(ProductRole.TENANT_ADMIN);
        when(userProductAccessRepository.findByUser(user)).thenReturn(List.of(crm, hrms));

        JwtPrincipal principal = jwtService.parsePrincipal(jwtService.generateAccessToken(user));

        assertThat(principal.products()).containsExactlyInAnyOrder("CRM", "HRMS");
        assertThat(principal.productRoles())
            .containsEntry("CRM", "SALES_MANAGER")
            .containsEntry("HRMS", "TENANT_ADMIN");
    }
}
