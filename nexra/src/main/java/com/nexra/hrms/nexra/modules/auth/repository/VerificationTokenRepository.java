package com.nexra.hrms.nexra.modules.auth.repository;

import com.nexra.hrms.nexra.modules.auth.entity.UserAccount;
import com.nexra.hrms.nexra.modules.auth.entity.VerificationToken;
import com.nexra.hrms.nexra.modules.auth.enums.VerificationPurpose;
import com.nexra.hrms.nexra.modules.auth.enums.VerificationType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

/**
 * Provides persistence operations for verification token records.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    /**
     * Finds verification token by unique user, purpose, type, and hash.
     *
     * @param user user account
     * @param purpose verification purpose
     * @param type verification type
     * @param tokenHash token hash
     * @return optional verification token
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<VerificationToken> findByUserAndPurposeAndTypeAndTokenHash(
        UserAccount user,
        VerificationPurpose purpose,
        VerificationType type,
        String tokenHash
    );

    /**
     * Deletes expired verification tokens for provided user.
     *
     * @param user user account
     * @param expiryCutoff expiry cut-off instant
     */
    void deleteByUserAndExpiresAtBefore(UserAccount user, Instant expiryCutoff);
}
