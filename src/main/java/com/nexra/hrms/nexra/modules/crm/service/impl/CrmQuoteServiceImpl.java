package com.nexra.hrms.nexra.modules.crm.service.impl;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.common.audit.AuditEventRecord;
import com.nexra.hrms.nexra.common.audit.AuditEventService;
import com.nexra.hrms.nexra.common.exception.NexraNotFoundException;
import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmQuoteCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmQuoteStatusUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.entity.CrmDealEntity;
import com.nexra.hrms.nexra.modules.crm.entity.CrmQuoteEntity;
import com.nexra.hrms.nexra.modules.crm.entity.CrmQuoteLineItemEntity;
import com.nexra.hrms.nexra.modules.crm.model.CrmQuote;
import com.nexra.hrms.nexra.modules.crm.repository.CrmDealRepository;
import com.nexra.hrms.nexra.modules.crm.repository.CrmQuoteLineItemRepository;
import com.nexra.hrms.nexra.modules.crm.repository.CrmQuoteRepository;
import com.nexra.hrms.nexra.modules.crm.service.CrmQuoteService;
import com.nexra.hrms.nexra.modules.operations.service.OpsProjectService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default CRM quote service. Computes line and quote totals with BigDecimal
 * precision, enforces a configurable quote lifecycle state machine, and records
 * audit events for quote creation and status transitions.
 *
 * @author niteshjaitwar
 */
@Service
@RequiredArgsConstructor
public class CrmQuoteServiceImpl implements CrmQuoteService {

    private static final String AUDIT_MODULE = "CRM";
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final CrmQuoteRepository quoteRepository;
    private final CrmQuoteLineItemRepository lineItemRepository;
    private final CrmDealRepository dealRepository;
    private final CrmProperties crmProperties;
    private final AuditEventService auditEventService;
    private final OpsProjectService opsProjectService;

    @Override
    @Transactional
    public CrmQuote create(final String tenantCode, final String actorEmail, final CrmQuoteCreateRequest request) {
        final String normalizedTenant = normalize(tenantCode);
        final CrmProperties.Quote config = crmProperties.getQuote();

        final String quoteId = UUID.randomUUID().toString();
        final CrmQuoteEntity quote = new CrmQuoteEntity();
        quote.setId(quoteId);
        quote.setTenantCode(normalizedTenant);
        quote.setQuoteNumber(buildQuoteNumber(normalizedTenant));
        quote.setTitle(request.title().trim());
        quote.setStatus(config.getDefaultStatus());
        quote.setCurrency(request.currency() == null || request.currency().isBlank()
            ? config.getDefaultCurrency()
            : request.currency().trim().toUpperCase(Locale.ROOT));
        quote.setDealId(trimNullable(request.dealId()));
        quote.setAccountId(trimNullable(request.accountId()));
        quote.setContactId(trimNullable(request.contactId()));
        quote.setOwnerUserId(request.ownerUserId().trim());
        quote.setValidUntil(request.validUntil());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountTotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        BigDecimal grandTotal = BigDecimal.ZERO;
        final List<CrmQuoteLineItemEntity> lineEntities = new ArrayList<>();

        int lineNo = 1;
        for (final CrmQuoteCreateRequest.LineItem line : request.lineItems()) {
            final BigDecimal discountPercent = nullToZero(line.discountPercent());
            final BigDecimal taxPercent = nullToZero(line.taxPercent());
            final BigDecimal gross = line.quantity().multiply(line.unitPrice());
            final BigDecimal discount = scale(gross.multiply(discountPercent).divide(HUNDRED, 2, RoundingMode.HALF_UP));
            final BigDecimal net = gross.subtract(discount);
            final BigDecimal tax = scale(net.multiply(taxPercent).divide(HUNDRED, 2, RoundingMode.HALF_UP));
            final BigDecimal lineTotal = scale(net.add(tax));

            final CrmQuoteLineItemEntity entity = new CrmQuoteLineItemEntity();
            entity.setId(UUID.randomUUID().toString());
            entity.setQuoteId(quoteId);
            entity.setTenantCode(normalizedTenant);
            entity.setLineNo(lineNo++);
            entity.setProductName(line.productName().trim());
            entity.setQuantity(scale(line.quantity()));
            entity.setUnitPrice(scale(line.unitPrice()));
            entity.setDiscountPercent(discountPercent);
            entity.setTaxPercent(taxPercent);
            entity.setLineTotal(lineTotal);
            lineEntities.add(entity);

            subtotal = subtotal.add(gross);
            discountTotal = discountTotal.add(discount);
            taxTotal = taxTotal.add(tax);
            grandTotal = grandTotal.add(lineTotal);
        }

        quote.setSubtotal(scale(subtotal));
        quote.setDiscountTotal(scale(discountTotal));
        quote.setTaxTotal(scale(taxTotal));
        quote.setGrandTotal(scale(grandTotal));

        final CrmQuoteEntity savedQuote = quoteRepository.save(quote);
        lineItemRepository.saveAll(lineEntities);

        audit(normalizedTenant, actorEmail, "CREATE_QUOTE", "SUCCESS", savedQuote.getId(),
            "{\"quoteNumber\":\"" + savedQuote.getQuoteNumber() + "\",\"grandTotal\":\"" + savedQuote.getGrandTotal() + "\"}");
        return toModel(savedQuote, lineEntities);
    }

    @Override
    @Transactional(readOnly = true)
    public CrmQuote findById(final String tenantCode, final String quoteId) {
        final CrmQuoteEntity quote = load(normalize(tenantCode), quoteId);
        return toModel(quote, lineItemRepository.findAllByQuoteIdOrderByLineNoAsc(quote.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CrmQuote> list(final String tenantCode, final int page, final int size) {
        final Page<CrmQuoteEntity> result = quoteRepository.findAllByTenantCodeIgnoreCaseOrderByUpdatedAtDescIdDesc(
            normalize(tenantCode),
            PageRequest.of(page, size)
        );
        final List<CrmQuote> items = result.getContent().stream()
            .map((quote) -> toModel(quote, lineItemRepository.findAllByQuoteIdOrderByLineNoAsc(quote.getId())))
            .toList();
        return new PageResponse<>(items, result.getNumber(), result.getSize(), result.getTotalElements(),
            result.getTotalPages(), result.hasNext(), result.hasPrevious());
    }

    @Override
    @Transactional
    public CrmQuote transitionStatus(
        final String tenantCode,
        final String actorEmail,
        final String quoteId,
        final CrmQuoteStatusUpdateRequest request
    ) {
        final String normalizedTenant = normalize(tenantCode);
        final CrmQuoteEntity quote = load(normalizedTenant, quoteId);
        final CrmProperties.Quote config = crmProperties.getQuote();
        final String current = quote.getStatus();
        final String target = request.targetStatus().trim().toUpperCase(Locale.ROOT);

        if (!config.isKnownStatus(target)) {
            audit(normalizedTenant, actorEmail, "QUOTE_STATUS_TRANSITION", "FAILURE", quote.getId(),
                "{\"reason\":\"UNKNOWN_STATUS\",\"target\":\"" + target + "\"}");
            throw new NexraValidationException("Unknown quote status: " + target);
        }
        if (!config.isTransitionAllowed(current, target)) {
            audit(normalizedTenant, actorEmail, "QUOTE_STATUS_TRANSITION", "FAILURE", quote.getId(),
                "{\"reason\":\"ILLEGAL_TRANSITION\",\"from\":\"" + current + "\",\"to\":\"" + target + "\"}");
            throw new NexraValidationException("Illegal quote status transition from " + current + " to " + target + ".");
        }

        quote.setStatus(target);
        CrmQuoteEntity saved = quoteRepository.save(quote);
        if ("ACCEPTED".equals(target)) {
            saved = syncDealFromAcceptedQuote(saved, normalizedTenant, actorEmail);
            maybeCreateOpsProject(saved, normalizedTenant);
        }
        audit(normalizedTenant, actorEmail, "QUOTE_STATUS_TRANSITION", "SUCCESS", saved.getId(),
            "{\"from\":\"" + current + "\",\"to\":\"" + target + "\"}");
        return toModel(saved, lineItemRepository.findAllByQuoteIdOrderByLineNoAsc(saved.getId()));
    }

    private CrmQuoteEntity syncDealFromAcceptedQuote(
        final CrmQuoteEntity quote,
        final String tenantCode,
        final String actorEmail
    ) {
        final String wonStage = crmProperties.getQuote()
            .resolveAcceptedDealStage(crmProperties.getWonDealStageNormalized());
        if (quote.getDealId() != null) {
            dealRepository.findByIdAndTenantCodeIgnoreCase(quote.getDealId(), tenantCode).ifPresent((deal) -> {
                deal.setStage(wonStage);
                deal.setValueAmount(quote.getGrandTotal());
                deal.setCurrency(quote.getCurrency());
                dealRepository.save(deal);
                auditEventService.record(AuditEventRecord
                    .of(tenantCode, AUDIT_MODULE, "SYNC_DEAL_FROM_QUOTE", "SUCCESS")
                    .withActor(actorEmail, null)
                    .withTarget("CRM_DEAL", deal.getId())
                    .withDetail("{\"quoteId\":\"" + quote.getId() + "\",\"stage\":\"" + wonStage + "\"}"));
            });
            return quote;
        }

        final CrmDealEntity deal = new CrmDealEntity();
        deal.setId(UUID.randomUUID().toString());
        deal.setTenantCode(tenantCode);
        deal.setAccountId(quote.getAccountId());
        deal.setContactId(quote.getContactId());
        deal.setTitle(quote.getTitle());
        deal.setStage(wonStage);
        deal.setValueAmount(quote.getGrandTotal());
        deal.setCurrency(quote.getCurrency());
        deal.setOwnerUserId(quote.getOwnerUserId());
        final CrmDealEntity savedDeal = dealRepository.save(deal);
        quote.setDealId(savedDeal.getId());
        final CrmQuoteEntity linked = quoteRepository.save(quote);
        auditEventService.record(AuditEventRecord
            .of(tenantCode, AUDIT_MODULE, "CREATE_DEAL_FROM_QUOTE", "SUCCESS")
            .withActor(actorEmail, null)
            .withTarget("CRM_DEAL", savedDeal.getId())
            .withDetail("{\"quoteId\":\"" + quote.getId() + "\",\"stage\":\"" + wonStage + "\"}"));
        return linked;
    }

    private void maybeCreateOpsProject(final CrmQuoteEntity quote, final String tenantCode) {
        if (!crmProperties.getQuote().isAutoCreateOpsProjectOnAccept() || quote.getDealId() == null) {
            return;
        }
        opsProjectService.createFromCrmDealIfAbsent(tenantCode, quote.getDealId(), quote.getOwnerUserId());
    }

    private CrmQuoteEntity load(final String tenantCode, final String quoteId) {
        return quoteRepository.findByIdAndTenantCodeIgnoreCase(quoteId, tenantCode)
            .orElseThrow(() -> new NexraNotFoundException("CRM quote not found for id: " + quoteId));
    }

    private String buildQuoteNumber(final String tenantCode) {
        final String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        return "Q-" + tenantCode.toUpperCase(Locale.ROOT) + "-" + suffix;
    }

    private void audit(
        final String tenantCode,
        final String actorEmail,
        final String action,
        final String outcome,
        final String quoteId,
        final String detailJson
    ) {
        auditEventService.record(AuditEventRecord
            .of(tenantCode, AUDIT_MODULE, action, outcome)
            .withActor(actorEmail, null)
            .withTarget("CRM_QUOTE", quoteId)
            .withDetail(detailJson));
    }

    private CrmQuote toModel(final CrmQuoteEntity quote, final List<CrmQuoteLineItemEntity> lineEntities) {
        final List<CrmQuote.LineItem> lines = lineEntities.stream()
            .map((line) -> new CrmQuote.LineItem(
                line.getId(),
                line.getLineNo(),
                line.getProductName(),
                line.getQuantity(),
                line.getUnitPrice(),
                line.getDiscountPercent(),
                line.getTaxPercent(),
                line.getLineTotal()
            ))
            .toList();
        return new CrmQuote(
            quote.getId(),
            quote.getTenantCode(),
            quote.getQuoteNumber(),
            quote.getTitle(),
            quote.getStatus(),
            quote.getCurrency(),
            quote.getDealId(),
            quote.getAccountId(),
            quote.getContactId(),
            quote.getOwnerUserId(),
            quote.getSubtotal(),
            quote.getDiscountTotal(),
            quote.getTaxTotal(),
            quote.getGrandTotal(),
            quote.getValidUntil(),
            lines
        );
    }

    private BigDecimal nullToZero(final BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal scale(final BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalize(final String tenantCode) {
        if (tenantCode == null || tenantCode.isBlank()) {
            throw new NexraValidationException("Tenant code is required.");
        }
        return tenantCode.trim();
    }

    private String trimNullable(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
