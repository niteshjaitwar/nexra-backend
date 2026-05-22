package com.nexra.hrms.nexra.modules.crm.model;

public record CrmLeadConversionResult(
    String leadId,
    String accountId,
    String contactId,
    String dealId
) {
}

