package com.nexra.hrms.nexra.modules.auth.config;

import com.nexra.hrms.nexra.modules.auth.entity.Tenant;
import com.nexra.hrms.nexra.modules.auth.entity.UserAccount;
import com.nexra.hrms.nexra.modules.auth.enums.AccountType;
import com.nexra.hrms.nexra.modules.auth.enums.UserRole;
import com.nexra.hrms.nexra.modules.auth.enums.UserStatus;
import com.nexra.hrms.nexra.modules.auth.repository.TenantRepository;
import com.nexra.hrms.nexra.modules.auth.repository.UserAccountRepository;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

/**
 * Bootstraps default tenant, OAuth client, and admin user records for first-run environments.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "app.auth.bootstrap", name = "enabled", havingValue = "true")
public class DataBootstrapConfig {

    private static final String BOOTSTRAP_ADMIN_PASSWORD = "Admin@1234";

    /**
     * Creates default tenant when it does not exist.
     *
     * @param tenantRepository tenant repository
     * @return startup runner
     */
    @Bean
    public CommandLineRunner bootstrapDefaultTenant(final TenantRepository tenantRepository) {
        return args -> {
            String defaultCode = "nexra";
            if (!tenantRepository.existsByCodeIgnoreCase(defaultCode)) {
                Tenant tenant = new Tenant();
                tenant.setCode(defaultCode);
                tenant.setName("Nexra Default Tenant");
                tenant.setEnterprise(true);
                tenant.setActive(true);
                tenantRepository.save(tenant);
                log.info("DataBootstrapConfig() - bootstrapDefaultTenant() - Default tenant created, code={}", defaultCode);
            }
        };
    }

    /**
     * Creates default OAuth client when it does not exist.
     * Registers scopes for both HRMS and CRM products.
     *
     * @param registeredClientRepository registered client repository
     * @param passwordEncoder password encoder
     * @param authProperties auth property container
     * @return startup runner
     */
    @Bean
    public CommandLineRunner bootstrapDefaultOAuthClient(
        final RegisteredClientRepository registeredClientRepository,
        final PasswordEncoder passwordEncoder,
        final AuthProperties authProperties
    ) {
        return args -> {
            String clientId = authProperties.getOauth2().getDefaultClientId();
            if (registeredClientRepository.findByClientId(clientId) == null) {
                RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId(clientId)
                    .clientIdIssuedAt(Instant.now())
                    .clientSecret(passwordEncoder.encode(authProperties.getOauth2().getDefaultClientSecret()))
                    .clientName("Nexra Default Web Client")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .redirectUri(authProperties.getOauth2().getDefaultRedirectUri())
                    .scope(OidcScopes.OPENID)
                    .scope("profile")
                    .scope("hrms.read")
                    .scope("hrms.write")
                    .scope("crm.read")
                    .scope("crm.write")
                    .clientSettings(ClientSettings.builder().requireProofKey(true).requireAuthorizationConsent(true).build())
                    .tokenSettings(TokenSettings.builder().reuseRefreshTokens(false).build())
                    .build();
                registeredClientRepository.save(client);
                log.info("DataBootstrapConfig() - bootstrapDefaultOAuthClient() - Default OAuth client created, clientId={}", clientId);
            }
        };
    }

    /**
     * Creates default admin user under nexra tenant when it does not exist.
     * This ensures a usable admin account is available on first run without manual seeding.
     *
     * @param tenantRepository tenant repository
     * @param userAccountRepository user account repository
     * @param passwordEncoder password encoder
     * @return startup runner
     */
    @Bean
    public CommandLineRunner bootstrapDefaultAdminUser(
        final TenantRepository tenantRepository,
        final UserAccountRepository userAccountRepository,
        final PasswordEncoder passwordEncoder
    ) {
        return args -> {
            String adminEmail = "admin@nexra.local";
            tenantRepository.findByCodeIgnoreCaseAndActiveTrue("nexra").ifPresent(tenant -> {
                if (!userAccountRepository.existsByTenantAndEmailIgnoreCase(tenant, adminEmail)) {
                    UserAccount admin = new UserAccount();
                    admin.setTenant(tenant);
                    admin.setEmail(adminEmail);
                    admin.setFirstName("Nexra");
                    admin.setLastName("Admin");
                    admin.setPasswordHash(passwordEncoder.encode(BOOTSTRAP_ADMIN_PASSWORD));
                    admin.setStatus(UserStatus.ACTIVE);
                    admin.setEmailVerified(true);
                    admin.setMfaEnabled(false);
                    admin.setAccountType(AccountType.ENTERPRISE);
                    admin.setRoles(Set.of(UserRole.ROLE_TENANT_ADMIN));
                    userAccountRepository.save(admin);
                    log.info("DataBootstrapConfig() - bootstrapDefaultAdminUser() - Default admin user created, email={}", adminEmail);
                }
            });
        };
    }
}
