package com.nexra.hrms.nexra.modules.auth.repository;

import com.nexra.hrms.nexra.modules.auth.entity.RefreshToken;
import com.nexra.hrms.nexra.modules.auth.entity.UserAccount;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Provides persistence operations for refresh token lifecycle records.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Finds refresh token by stored hash value.
     *
     * @param tokenHash token hash
     * @return optional refresh token
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Deletes expired refresh tokens for provided user.
     *
     * @param user user account
     * @param expiryCutoff expiry cut-off instant
     */
    void deleteByUserAndExpiresAtBefore(UserAccount user, Instant expiryCutoff);

    /**
     * Bulk-revokes all active refresh tokens for a user in a single query.
     * Replaces N individual save operations to avoid N+1 performance issue.
     *
     * @param user user account
     * @param now revocation timestamp
     */
    @Modifying
    @Query("UPDATE RefreshToken t SET t.revokedAt = :now WHERE t.user = :user AND t.revokedAt IS NULL")
    void revokeAllActiveByUser(@Param("user") UserAccount user, @Param("now") Instant now);
}
