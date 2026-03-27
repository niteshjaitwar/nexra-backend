package com.nexra.hrms.nexra.modules.auth.config.dev;

import com.nexra.hrms.nexra.modules.auth.entity.Tenant;
import com.nexra.hrms.nexra.modules.auth.entity.UserAccount;
import com.nexra.hrms.nexra.modules.auth.enums.AccountType;
import com.nexra.hrms.nexra.modules.auth.enums.UserRole;
import com.nexra.hrms.nexra.modules.auth.enums.UserStatus;
import com.nexra.hrms.nexra.modules.auth.repository.TenantRepository;
import com.nexra.hrms.nexra.modules.auth.repository.UserAccountRepository;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds deterministic development data for local testing and API verification.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Slf4j
@Configuration
@Profile({"dev", "e2e"})
public class DevDataSeederConfig {

    private static final String DEFAULT_TENANT_CODE = "nexra";
    private static final String DEFAULT_TENANT_NAME = "Nexra Default Tenant";
    private static final String DEV_PASSWORD = "Password@1234";
    private static final String DEV_ADMIN_EMAIL = "dev.admin@nexra.local";
    private static final int MIN_DEV_USERS = 10;

    /**
     * Seeds default tenant, admin user, and minimum baseline users for development profile.
     *
     * @param tenantRepository tenant repository
     * @param userAccountRepository user repository
     * @param passwordEncoder password encoder
     * @return startup runner
     */
    @Bean
    public CommandLineRunner seedMinimumUsersForDev(
        final TenantRepository tenantRepository,
        final UserAccountRepository userAccountRepository,
        final PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Tenant tenant = tenantRepository.findByCodeIgnoreCaseAndActiveTrue(DEFAULT_TENANT_CODE)
                .orElseGet(() -> {
                    Tenant t = new Tenant();
                    t.setCode(DEFAULT_TENANT_CODE);
                    t.setName(DEFAULT_TENANT_NAME);
                    t.setEnterprise(true);
                    t.setActive(true);
                    return tenantRepository.save(t);
                });

            if (!userAccountRepository.existsByTenantAndEmailIgnoreCase(tenant, DEV_ADMIN_EMAIL)) {
                UserAccount admin = new UserAccount();
                admin.setTenant(tenant);
                admin.setEmail(DEV_ADMIN_EMAIL);
                admin.setPasswordHash(passwordEncoder.encode(DEV_PASSWORD));
                admin.setFirstName("Dev");
                admin.setLastName("Admin");
                admin.setEmailVerified(true);
                admin.setMfaEnabled(false);
                admin.setAccountType(AccountType.ENTERPRISE);
                admin.setStatus(UserStatus.ACTIVE);
                admin.setRoles(Set.of(UserRole.ROLE_USER, UserRole.ROLE_PLATFORM_ADMIN));
                userAccountRepository.save(admin);
                log.info("DevDataSeederConfig() - seedMinimumUsersForDev() - Dev admin user seeded");
            }

            for (int index = 1; index <= MIN_DEV_USERS; index++) {
                String email = "dev.user" + index + "@nexra.local";
                if (userAccountRepository.existsByTenantAndEmailIgnoreCase(tenant, email)) {
                    continue;
                }
                UserAccount user = new UserAccount();
                user.setTenant(tenant);
                user.setEmail(email);
                user.setPasswordHash(passwordEncoder.encode(DEV_PASSWORD));
                user.setFirstName("Dev" + index);
                user.setLastName("User");
                user.setEmailVerified(true);
                user.setMfaEnabled(false);
                user.setAccountType(AccountType.ENTERPRISE);
                user.setStatus(UserStatus.ACTIVE);
                user.setRoles(Set.of(UserRole.ROLE_USER));
                userAccountRepository.save(user);
            }

            long tenantUserCount = userAccountRepository.countByTenant(tenant);
            log.info("DevDataSeederConfig() - seedMinimumUsersForDev() - Dev records seeded successfully, tenantCode={}, tenantUserCount={}",
                tenant.getCode(), tenantUserCount);
        };
    }
}
