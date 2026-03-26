package com.nexra.hrms.nexra.modules.auth.entity;

import com.nexra.hrms.nexra.modules.auth.entity.base.AbstractAuditableEntity;
import com.nexra.hrms.nexra.modules.auth.enums.VerificationPurpose;
import com.nexra.hrms.nexra.modules.auth.enums.VerificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * Stores OTP and link verification secrets as hashes with expiry and consumption metadata.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Entity
@Table(name = "verification_tokens")
@Getter
@Setter
public class VerificationToken extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private VerificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private VerificationPurpose purpose;

    @Column(nullable = false, length = 128)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column
    private Instant consumedAt;

    @Column(nullable = false, length = 160)
    private String deliveryTarget;
}
