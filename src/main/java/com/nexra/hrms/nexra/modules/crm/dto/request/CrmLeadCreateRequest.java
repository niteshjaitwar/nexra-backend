package com.nexra.hrms.nexra.modules.crm.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating CRM leads.
 *
 * @param fullName lead full name.
 * @param email lead email address.
 * @param phone lead phone number.
 * @param company lead company name.
 * @param source lead acquisition source.
 * @param ownerUserId assigned owner user id.
 * @param notes optional notes.
 * @author niteshjaitwar
 * @version 1.0
 */
public record CrmLeadCreateRequest(
    @NotBlank(message = "Full name is required.")
    @Size(max = 120, message = "Full name must not exceed 120 characters.")
    String fullName,

    @NotBlank(message = "Email is required.")
    @Email(message = "Email must be valid.")
    @Size(max = 160, message = "Email must not exceed 160 characters.")
    String email,

    @Size(max = 32, message = "Phone must not exceed 32 characters.")
    String phone,

    @NotBlank(message = "Company is required.")
    @Size(max = 120, message = "Company must not exceed 120 characters.")
    String company,

    @Size(max = 80, message = "Source must not exceed 80 characters.")
    String source,

    @Size(max = 36, message = "Campaign id must not exceed 36 characters.")
    String campaignId,

    @NotBlank(message = "Owner user id is required.")
    @Size(max = 120, message = "Owner user id must not exceed 120 characters.")
    String ownerUserId,

    @Size(max = 1000, message = "Notes must not exceed 1000 characters.")
    String notes
) {
}
