package com.nexra.hrms.nexra.common.persistence;

import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resolves the current auditor for Spring Data auditing. Reads the
 * authenticated principal from the SecurityContextHolder and extracts a
 * stable, short identifier (email or userId) that fits the created_by
 * and updated_by VARCHAR(120) columns. Falls back to the literal string
 * "system" when no authenticated context exists, for example during
 * Flyway bootstrapping or asynchronous background jobs.
 *
 * @author niteshjaitwar
 */
@Component("auditorAwareResolver")
public class AuditorAwareResolver implements AuditorAware<String> {

    private static final String SYSTEM_AUDITOR = "system";
    private static final String ANONYMOUS_USER = "anonymousUser";
    private static final int MAX_AUDITOR_LENGTH = 120;

    /**
     * Returns the current auditor name from the security context. For a
     * JwtPrincipal we prefer the email, otherwise the userId, never the
     * record's toString. For all other principals we fall back to the
     * authentication name and defensively truncate it so auditor values
     * always fit the database column.
     *
     * @return optional auditor identifier, always present, defaulting to "system".
     */
    @Override
    public Optional<String> getCurrentAuditor() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of(SYSTEM_AUDITOR);
        }

        final Object principal = authentication.getPrincipal();
        if (principal instanceof JwtPrincipal jwt) {
            final String candidate = jwt.email() != null && !jwt.email().isBlank()
                    ? jwt.email()
                    : jwt.userId() != null ? jwt.userId().toString() : null;
            if (candidate != null && !candidate.isBlank()) {
                return Optional.of(truncate(candidate));
            }
        }

        final String name = authentication.getName();
        if (name == null || name.isBlank() || ANONYMOUS_USER.equals(name)) {
            return Optional.of(SYSTEM_AUDITOR);
        }
        return Optional.of(truncate(name));
    }

    /**
     * Truncates the auditor identifier to the maximum allowed column width.
     *
     * @param value raw auditor identifier.
     * @return value trimmed to MAX_AUDITOR_LENGTH characters.
     */
    private String truncate(final String value) {
        return value.length() <= MAX_AUDITOR_LENGTH ? value : value.substring(0, MAX_AUDITOR_LENGTH);
    }
}
