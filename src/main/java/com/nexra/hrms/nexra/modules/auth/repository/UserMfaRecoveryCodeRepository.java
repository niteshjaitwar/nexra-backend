package com.nexra.hrms.nexra.modules.auth.repository;

import com.nexra.hrms.nexra.modules.auth.entity.UserMfaRecoveryCodeEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserMfaRecoveryCodeRepository extends JpaRepository<UserMfaRecoveryCodeEntity, UUID> {

    List<UserMfaRecoveryCodeEntity> findByUserIdAndUsedAtIsNull(UUID userId);

    Optional<UserMfaRecoveryCodeEntity> findByUserIdAndCodeHashAndUsedAtIsNull(UUID userId, String codeHash);

    @Modifying
    @Query("DELETE FROM UserMfaRecoveryCodeEntity c WHERE c.userId = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);
}
