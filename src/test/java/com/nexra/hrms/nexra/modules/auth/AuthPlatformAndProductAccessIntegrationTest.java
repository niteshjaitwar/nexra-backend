package com.nexra.hrms.nexra.modules.auth;

import com.nexra.hrms.nexra.modules.auth.entity.Tenant;
import com.nexra.hrms.nexra.modules.auth.entity.UserAccount;
import com.nexra.hrms.nexra.modules.auth.enums.AccountType;
import com.nexra.hrms.nexra.modules.auth.enums.UserRole;
import com.nexra.hrms.nexra.modules.auth.enums.UserStatus;
import com.nexra.hrms.nexra.modules.auth.repository.RefreshTokenRepository;
import com.nexra.hrms.nexra.modules.auth.repository.TenantRepository;
import com.nexra.hrms.nexra.modules.auth.repository.UserAccountRepository;
import com.nexra.hrms.nexra.modules.auth.repository.UserProductAccessRepository;
import com.nexra.hrms.nexra.modules.auth.repository.VerificationTokenRepository;
import com.nexra.hrms.nexra.modules.auth.security.JwtService;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end coverage for platform tenant provisioning and tenant-admin product access APIs.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Platform provisioning & product access")
class AuthPlatformAndProductAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserProductAccessRepository userProductAccessRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void clean() {
        verificationTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userProductAccessRepository.deleteAll();
        userAccountRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/platform/tenants/provision creates tenant and admin (platform admin only)")
    void shouldProvisionTenantWhenPlatformAdmin() throws Exception {
        String code = "sme" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String body = """
            {
              "tenantCode": "%s",
              "companyName": "SME Test Co",
              "adminEmail": "admin.%s@example.com",
              "adminFirstName": "Admin",
              "adminLastName": "User",
              "products": ["HRMS"]
            }
            """.formatted(code, code);

        mockMvc.perform(post("/api/v1/platform/tenants/provision")
                .with(user("platform").roles("PLATFORM_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.tenantCode").value(code.toLowerCase()))
            .andExpect(jsonPath("$.data.grantedProducts[0]").value("HRMS"));
    }

    @Test
    @DisplayName("POST /api/v1/platform/tenants/provision returns 409 when tenant code exists")
    void shouldRejectDuplicateTenantProvision() throws Exception {
        String code = "dup" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String body = """
            {
              "tenantCode": "%s",
              "companyName": "Dup Co",
              "adminEmail": "a1.%s@example.com",
              "adminFirstName": "A",
              "adminLastName": "B",
              "products": ["HRMS"]
            }
            """.formatted(code, code);

        mockMvc.perform(post("/api/v1/platform/tenants/provision")
                .with(user("platform").roles("PLATFORM_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/platform/tenants/provision")
                .with(user("platform").roles("PLATFORM_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.replace("a1.", "a2.")))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Tenant admin JWT can list, grant, and revoke product access")
    void shouldManageProductAccessWithTenantAdminBearerToken() throws Exception {
        Tenant tenant = new Tenant();
        tenant.setCode("pa" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        tenant.setName("Product Access Tenant");
        tenant.setEnterprise(true);
        tenant.setActive(true);
        tenant = tenantRepository.save(tenant);

        UserAccount admin = new UserAccount();
        admin.setTenant(tenant);
        admin.setEmail("tadmin@" + tenant.getCode() + ".local");
        admin.setPasswordHash(passwordEncoder.encode("Password@123"));
        admin.setFirstName("T");
        admin.setLastName("Admin");
        admin.setEmailVerified(true);
        admin.setMfaEnabled(false);
        admin.setAccountType(AccountType.ENTERPRISE);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setRoles(Set.of(UserRole.ROLE_TENANT_ADMIN));
        admin = userAccountRepository.save(admin);

        UserAccount loaded = userAccountRepository.findById(admin.getId()).orElseThrow();
        String bearer = jwtService.generateAccessToken(loaded);

        mockMvc.perform(get("/api/v1/admin/users/" + admin.getId() + "/products")
                .header("Authorization", "Bearer " + bearer))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(post("/api/v1/admin/users/" + admin.getId() + "/products")
                .header("Authorization", "Bearer " + bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"product":"CRM","productRole":"SALES_REP"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.product").value("CRM"));

        mockMvc.perform(get("/api/v1/admin/users/" + admin.getId() + "/products")
                .header("Authorization", "Bearer " + bearer))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)));

        mockMvc.perform(delete("/api/v1/admin/users/" + admin.getId() + "/products/CRM")
                .header("Authorization", "Bearer " + bearer))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/admin/users/" + admin.getId() + "/products")
                .header("Authorization", "Bearer " + bearer))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("Regular user JWT cannot grant product access (403)")
    void shouldRejectProductAccessGrantForRegularUser() throws Exception {
        Tenant tenant = new Tenant();
        tenant.setCode("rg" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        tenant.setName("Regular User Tenant");
        tenant.setEnterprise(true);
        tenant.setActive(true);
        tenant = tenantRepository.save(tenant);

        UserAccount user = new UserAccount();
        user.setTenant(tenant);
        user.setEmail("plain@" + tenant.getCode() + ".local");
        user.setPasswordHash(passwordEncoder.encode("Password@123"));
        user.setFirstName("U");
        user.setLastName("Ser");
        user.setEmailVerified(true);
        user.setMfaEnabled(false);
        user.setAccountType(AccountType.ENTERPRISE);
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Set.of(UserRole.ROLE_USER));
        user = userAccountRepository.save(user);

        UserAccount loaded = userAccountRepository.findById(user.getId()).orElseThrow();
        String bearer = jwtService.generateAccessToken(loaded);

        mockMvc.perform(post("/api/v1/admin/users/" + user.getId() + "/products")
                .header("Authorization", "Bearer " + bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"product":"HRMS","productRole":"EMPLOYEE"}
                    """))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Tenant admin cannot grant a product role that is incompatible with the product")
    void shouldRejectProductAccessGrantWhenRoleDoesNotBelongToProduct() throws Exception {
        Tenant tenant = createTenant("pr");
        UserAccount admin = createUser(tenant, "admin@" + tenant.getCode() + ".local", UserRole.ROLE_TENANT_ADMIN);
        UserAccount user = createUser(tenant, "user@" + tenant.getCode() + ".local", UserRole.ROLE_USER);
        String bearer = jwtService.generateAccessToken(userAccountRepository.findById(admin.getId()).orElseThrow());

        mockMvc.perform(post("/api/v1/admin/users/" + user.getId() + "/products")
                .header("Authorization", "Bearer " + bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"product":"HRMS","productRole":"SALES_REP"}
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    @DisplayName("Tenant admin JWT cannot manage product access for another tenant")
    void shouldRejectCrossTenantProductAccessManagement() throws Exception {
        Tenant tenantA = createTenant("ta");
        Tenant tenantB = createTenant("tb");
        UserAccount adminA = createUser(tenantA, "admin@" + tenantA.getCode() + ".local", UserRole.ROLE_TENANT_ADMIN);
        UserAccount userB = createUser(tenantB, "user@" + tenantB.getCode() + ".local", UserRole.ROLE_USER);
        String bearer = jwtService.generateAccessToken(userAccountRepository.findById(adminA.getId()).orElseThrow());

        mockMvc.perform(get("/api/v1/admin/users/" + userB.getId() + "/products")
                .header("Authorization", "Bearer " + bearer))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/admin/users/" + userB.getId() + "/products")
                .header("Authorization", "Bearer " + bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"product":"HRMS","productRole":"EMPLOYEE"}
                    """))
            .andExpect(status().isForbidden());
    }

    private Tenant createTenant(final String prefix) {
        Tenant tenant = new Tenant();
        tenant.setCode(prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        tenant.setName("Tenant " + prefix);
        tenant.setEnterprise(true);
        tenant.setActive(true);
        return tenantRepository.save(tenant);
    }

    private UserAccount createUser(final Tenant tenant, final String email, final UserRole role) {
        UserAccount user = new UserAccount();
        user.setTenant(tenant);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("Password@123"));
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmailVerified(true);
        user.setMfaEnabled(false);
        user.setAccountType(AccountType.ENTERPRISE);
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Set.of(role));
        return userAccountRepository.save(user);
    }
}
