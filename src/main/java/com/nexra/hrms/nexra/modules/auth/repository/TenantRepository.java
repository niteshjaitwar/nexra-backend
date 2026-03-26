package com.nexra.hrms.nexra.modules.auth.repository;

import com.nexra.hrms.nexra.modules.auth.entity.Tenant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides persistence operations for tenant entities.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    /**
     * Finds active tenant by code.
     *
     * @param code tenant code
     * @return optional tenant
     */
    Optional<Tenant> findByCodeIgnoreCaseAndActiveTrue(String code);

    /**
     * Checks whether tenant code already exists.
     *
     * @param code tenant code
     * @return true when tenant exists
     */
    boolean existsByCodeIgnoreCase(String code);
}
