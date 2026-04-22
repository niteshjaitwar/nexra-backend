package com.nexra.hrms.nexra.modules.crm.model;

import java.time.Instant;

/**
 * Immutable CRM lead aggregate used by service and API layers.
 *
 * @param id lead identifier.
 * @param tenantCode tenant code owning the lead.
 * @param fullName lead full name.
 * @param email lead email address.
 * @param phone lead phone number.
 * @param company lead company name.
 * @param source lead acquisition source.
 * @param ownerUserId assigned owner user id.
 * @param notes free-form notes.
 * @param status lead lifecycle status.
 * @param createdAt creation timestamp.
 * @param updatedAt update timestamp.
 * @author niteshjaitwar
 * @version 1.0
 */
public record CrmLead(
    String id,
    String tenantCode,
    String fullName,
    String email,
    String phone,
    String company,
    String source,
    String ownerUserId,
    String notes,
    CrmLeadStatus status,
    Instant createdAt,
    Instant updatedAt
) {
}
