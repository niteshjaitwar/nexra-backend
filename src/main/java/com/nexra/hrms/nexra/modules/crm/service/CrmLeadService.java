package com.nexra.hrms.nexra.modules.crm.service;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmLeadCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmLeadUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmLead;

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
    CrmLead create(String tenantCode, CrmLeadCreateRequest request);

    /**
     * Updates an existing CRM lead.
     *
     * @param leadId lead id.
     * @param request lead update payload.
     * @return updated lead.
     */
    CrmLead update(String tenantCode, String leadId, CrmLeadUpdateRequest request);

    /**
     * Retrieves a CRM lead by id.
     *
     * @param leadId lead id.
     * @return lead details.
     */
    CrmLead findById(String tenantCode, String leadId);

    /**
     * Lists CRM leads with pagination.
     *
     * @param page zero-based page index.
     * @param size requested page size.
     * @return paged lead collection.
     */
    PageResponse<CrmLead> list(String tenantCode, int page, int size);

    /**
     * Deletes a CRM lead by id.
     *
     * @param leadId lead id.
     */
    void delete(String tenantCode, String leadId);
}
