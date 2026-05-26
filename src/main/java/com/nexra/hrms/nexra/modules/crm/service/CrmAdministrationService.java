package com.nexra.hrms.nexra.modules.crm.service;

import com.nexra.hrms.nexra.modules.crm.dto.request.CrmCustomFieldDefinitionRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmRecordSharingRuleRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmWorkflowRuleRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.IntegrationWebhookSubscriptionRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmCustomFieldDefinition;
import com.nexra.hrms.nexra.modules.crm.model.CrmRecordSharingRule;
import com.nexra.hrms.nexra.modules.crm.model.CrmWorkflowRule;
import com.nexra.hrms.nexra.modules.crm.model.IntegrationWebhookSubscription;

import java.util.List;

public interface CrmAdministrationService {

    CrmCustomFieldDefinition createCustomField(String tenantCode, String actorEmail, String actorUserId, CrmCustomFieldDefinitionRequest request);

    List<CrmCustomFieldDefinition> listCustomFields(String tenantCode, String moduleKey);

    CrmWorkflowRule createWorkflowRule(String tenantCode, String actorEmail, String actorUserId, CrmWorkflowRuleRequest request);

    List<CrmWorkflowRule> listWorkflowRules(String tenantCode, String moduleKey);

    CrmRecordSharingRule createRecordSharingRule(String tenantCode, String actorEmail, String actorUserId, CrmRecordSharingRuleRequest request);

    List<CrmRecordSharingRule> listRecordSharingRules(String tenantCode, String moduleKey);

    IntegrationWebhookSubscription createWebhook(
        String tenantCode,
        String actorEmail,
        String actorUserId,
        IntegrationWebhookSubscriptionRequest request
    );

    List<IntegrationWebhookSubscription> listWebhooks(String tenantCode);
}
