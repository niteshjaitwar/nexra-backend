package com.nexra.hrms.nexra.modules.crm.service.impl;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.common.exception.NexraNotFoundException;
import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmDealCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmDealUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.entity.CrmDealEntity;
import com.nexra.hrms.nexra.modules.crm.model.CrmDeal;
import com.nexra.hrms.nexra.modules.crm.repository.CrmDealRepository;
import com.nexra.hrms.nexra.modules.crm.service.CrmDealService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CrmDealServiceImpl implements CrmDealService {

    private final CrmDealRepository repository;
    private final CrmProperties properties;

    @Override
    public CrmDeal create(final String tenantCode, final CrmDealCreateRequest request) {
        final CrmDealEntity entity = new CrmDealEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantCode(normalize(tenantCode));
        entity.setAccountId(normalizeNullable(request.accountId()));
        entity.setContactId(normalizeNullable(request.contactId()));
        entity.setTitle(normalize(request.title()));
        entity.setStage(normalize(request.stage()));
        entity.setValueAmount(request.valueAmount() != null ? request.valueAmount() : BigDecimal.ZERO);
        entity.setCurrency(normalizeNullable(request.currency()));
        entity.setOwnerUserId(normalize(request.ownerUserId()));
        entity.setExpectedCloseDate(request.expectedCloseDate());
        return toModel(repository.save(entity));
    }

    @Override
    public CrmDeal update(final String tenantCode, final String dealId, final CrmDealUpdateRequest request) {
        final CrmDealEntity entity = repository.findByIdAndTenantCodeIgnoreCase(dealId, normalize(tenantCode))
            .orElseThrow(() -> new NexraNotFoundException("CRM deal not found for id: " + dealId));
        entity.setAccountId(valueOrDefaultNullable(request.accountId(), entity.getAccountId()));
        entity.setContactId(valueOrDefaultNullable(request.contactId(), entity.getContactId()));
        entity.setTitle(valueOrDefault(request.title(), entity.getTitle()));
        entity.setStage(valueOrDefault(request.stage(), entity.getStage()));
        entity.setValueAmount(request.valueAmount() != null ? request.valueAmount() : entity.getValueAmount());
        entity.setCurrency(valueOrDefaultNullable(request.currency(), entity.getCurrency()));
        entity.setOwnerUserId(valueOrDefault(request.ownerUserId(), entity.getOwnerUserId()));
        entity.setExpectedCloseDate(request.expectedCloseDate() != null ? request.expectedCloseDate() : entity.getExpectedCloseDate());
        return toModel(repository.save(entity));
    }

    @Override
    public CrmDeal findById(final String tenantCode, final String dealId) {
        return toModel(repository.findByIdAndTenantCodeIgnoreCase(dealId, normalize(tenantCode))
            .orElseThrow(() -> new NexraNotFoundException("CRM deal not found for id: " + dealId)));
    }

    @Override
    public PageResponse<CrmDeal> list(final String tenantCode, final int page, final int size) {
        validatePaging(page, size);
        final Page<CrmDealEntity> result = repository.findAllByTenantCodeIgnoreCaseOrderByUpdatedAtDescIdDesc(
            normalize(tenantCode), PageRequest.of(page, size));
        final List<CrmDeal> items = result.getContent().stream().map(this::toModel).toList();
        return new PageResponse<>(items, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages(), result.hasNext(), result.hasPrevious());
    }

    @Override
    public void delete(final String tenantCode, final String dealId) {
        final CrmDealEntity entity = repository.findByIdAndTenantCodeIgnoreCase(dealId, normalize(tenantCode))
            .orElseThrow(() -> new NexraNotFoundException("CRM deal not found for id: " + dealId));
        repository.delete(entity);
    }

    private void validatePaging(final int page, final int size) {
        if (page < 0) {
            throw new NexraValidationException("Page index must be zero or greater.");
        }
        if (size <= 0 || size > properties.getMaxPageSize()) {
            throw new NexraValidationException("Page size must be between 1 and " + properties.getMaxPageSize() + ".");
        }
    }

    private String normalize(final String value) {
        if (value == null || value.isBlank()) {
            throw new NexraValidationException("Required field is missing.");
        }
        return value.trim();
    }

    private String normalizeNullable(final String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private String valueOrDefault(final String candidate, final String existing) {
        return candidate != null ? normalize(candidate) : existing;
    }

    private String valueOrDefaultNullable(final String candidate, final String existing) {
        return candidate != null ? normalizeNullable(candidate) : existing;
    }

    private CrmDeal toModel(final CrmDealEntity entity) {
        return new CrmDeal(
            entity.getId(),
            entity.getTenantCode(),
            entity.getAccountId(),
            entity.getContactId(),
            entity.getTitle(),
            entity.getStage(),
            entity.getValueAmount(),
            entity.getCurrency(),
            entity.getOwnerUserId(),
            entity.getExpectedCloseDate(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}

