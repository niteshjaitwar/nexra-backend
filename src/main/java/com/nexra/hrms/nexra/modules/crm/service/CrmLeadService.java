package com.nexra.hrms.nexra.modules.crm.service;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmLeadConvertRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmLeadCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmLeadUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmLead;
import com.nexra.hrms.nexra.modules.crm.model.CrmLeadConversionResult;
import com.nexra.hrms.nexra.modules.crm.support.CrmAccessScope;

/**
 * Business contract for managing CRM leads.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public interface CrmLeadService {

    /**
     * Creates a new CRM lead.
     *
     * @param request lead creation payload.
     * @return created lead.
     */
    CrmLead create(String tenantCode, CrmLeadCreateRequest request, CrmAccessScope accessScope);

    /**
     * Updates an existing CRM lead.
     *
     * @param leadId lead id.
     * @param request lead update payload.
     * @return updated lead.
     */
    CrmLead update(String tenantCode, String leadId, CrmLeadUpdateRequest request, CrmAccessScope accessScope);

    /**
     * Retrieves a CRM lead by id.
     *
     * @param leadId lead id.
     * @return lead details.
     */
    CrmLead findById(String tenantCode, String leadId, CrmAccessScope accessScope);

    /**
     * Lists CRM leads with pagination.
     *
     * @param page zero-based page index.
     * @param size requested page size.
     * @return paged lead collection.
     */
    PageResponse<CrmLead> list(String tenantCode, int page, int size, CrmAccessScope accessScope);

    /**
     * Lists leads attributed to a specific marketing campaign.
     *
     * @param tenantCode tenant scope.
     * @param campaignId campaign identifier.
     * @param page zero-based page index.
     * @param size requested page size.
     * @param accessScope caller access scope.
     * @return paged lead collection.
     */
    PageResponse<CrmLead> listByCampaign(
        String tenantCode,
        String campaignId,
        int page,
        int size,
        CrmAccessScope accessScope
    );

    /**
     * Converts a lead into account, contact, and deal records.
     *
     * @param tenantCode tenant scope.
     * @param leadId lead to convert.
     * @param request conversion payload.
     * @return identifiers for converted CRM records.
     */
    CrmLeadConversionResult convertLead(String tenantCode, String leadId, CrmLeadConvertRequest request, CrmAccessScope accessScope);

    /**
     * Deletes a CRM lead by id.
     *
     * @param leadId lead id.
     */
    void delete(String tenantCode, String leadId, CrmAccessScope accessScope);
}
