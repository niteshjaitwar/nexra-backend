package com.nexra.hrms.nexra.modules.auth.entity;

import com.nexra.hrms.nexra.modules.auth.entity.base.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Stores tenant metadata used to isolate authentication data for each organization.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Entity
@Table(name = "tenants")
@Getter
@Setter
public class Tenant extends AbstractAuditableEntity {

    @Column(nullable = false, unique = true, length = 60)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private boolean enterprise;

    @Column(nullable = false)
    private boolean active;
}
