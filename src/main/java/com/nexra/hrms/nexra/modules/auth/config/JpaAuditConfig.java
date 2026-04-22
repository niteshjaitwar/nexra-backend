package com.nexra.hrms.nexra.modules.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Activates Spring Data JPA auditing platform wide. Delegates auditor resolution
 * to the common AuditorAwareResolver so createdBy and updatedBy fields on the
 * shared BaseAuditableEntity are populated with the authenticated principal,
 * falling back to the literal string "system" for background jobs.
 *
 * @author niteshjaitwar
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAwareResolver")
public class JpaAuditConfig {
}
