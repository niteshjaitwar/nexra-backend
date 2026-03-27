package com.nexra.hrms.nexra.modules.auth.repository;

import com.nexra.hrms.nexra.modules.auth.entity.Tenant;
import com.nexra.hrms.nexra.modules.auth.entity.UserAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides persistence operations for tenant-scoped user accounts.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    /**
     * Finds user by tenant and email.
     *
     * @param tenant tenant entity
     * @param email user email
     * @return optional user account
     */
    Optional<UserAccount> findByTenantAndEmailIgnoreCase(Tenant tenant, String email);

    /**
     * Checks user existence for tenant and email.
     *
     * @param tenant tenant entity
     * @param email user email
     * @return true when user exists
     */
    boolean existsByTenantAndEmailIgnoreCase(Tenant tenant, String email);

    /**
     * Counts users belonging to tenant.
     *
     * @param tenant tenant entity
     * @return user count
     */
    long countByTenant(Tenant tenant);
}
