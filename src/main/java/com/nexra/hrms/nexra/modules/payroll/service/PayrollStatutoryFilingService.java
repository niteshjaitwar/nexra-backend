package com.nexra.hrms.nexra.modules.payroll.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexra.hrms.nexra.common.audit.AuditEventRecord;
import com.nexra.hrms.nexra.common.audit.AuditEventService;
import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.modules.payroll.config.PayrollProperties;
import com.nexra.hrms.nexra.modules.payroll.exception.PayrollBusinessException;
import com.nexra.hrms.nexra.modules.payroll.entity.PayrollStatutoryFilingEntity;
import com.nexra.hrms.nexra.modules.payroll.model.PayrollStatutoryFiling;
import com.nexra.hrms.nexra.modules.payroll.repository.PayrollStatutoryFilingRepository;
import com.nexra.hrms.nexra.modules.payroll.service.PayrollStatutoryService.StatutoryBreakdown;
import com.nexra.hrms.nexra.modules.payroll.service.PayrollStatutoryService.StatutoryComponentAmount;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generates and lists immutable statutory filing artifacts. A filing aggregates
 * the statutory contributions of every employee gross in the period using the
 * country pack resolved by {@link PayrollStatutoryService}, producing a record
 * suitable for downstream statutory submission (PF ECR, Form 941, RTI FPS).
 *
 * @author niteshjaitwar
 */
@Service
@RequiredArgsConstructor
public class PayrollStatutoryFilingService {

    private static final String STATUS_GENERATED = "GENERATED";
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_LOCKED = "LOCKED";
    private static final String AUDIT_MODULE = "PAYROLL";

    private final PayrollStatutoryFilingRepository filingRepository;
    private final PayrollStatutoryService statutoryService;
    private final PayrollProperties payrollProperties;
    private final AuditEventService auditEventService;
    private final ObjectMapper objectMapper;

    /**
     * Generates a statutory filing for the period from the supplied employee
     * gross amounts. Rejects duplicate filings for the same tenant, country,
     * period, and filing type.
     *
     * @param tenantCode   owning tenant.
     * @param countryCode  statutory country.
     * @param period       pay period (YYYY-MM).
     * @param grossAmounts per-employee gross monthly wages.
     * @param actorEmail   acting user email.
     * @return persisted filing artifact.
     */
    @Transactional
    public PayrollStatutoryFiling generate(
        final String tenantCode,
        final String countryCode,
        final String period,
        final List<BigDecimal> grossAmounts,
        final String actorEmail
    ) {
        final String normalizedTenant = require(tenantCode, "Tenant code is required.");
        final String normalizedCountry = require(countryCode, "Country code is required.").toUpperCase(Locale.ROOT);
        if (grossAmounts == null || grossAmounts.isEmpty()) {
            throw new PayrollBusinessException("At least one gross amount is required to generate a filing.");
        }
        final String filingType = payrollProperties.getStatutory().resolveFilingType(normalizedCountry);

        filingRepository
            .findByTenantCodeIgnoreCaseAndCountryCodeIgnoreCaseAndPeriodAndFilingType(
                normalizedTenant, normalizedCountry, period, filingType)
            .ifPresent((existing) -> {
                throw new PayrollBusinessException(
                    "A " + filingType + " filing already exists for " + normalizedCountry + " " + period);
            });

        final Map<String, BigDecimal> componentTotals = new LinkedHashMap<>();
        final Map<String, Boolean> employerFlags = new LinkedHashMap<>();
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal employeeContribution = BigDecimal.ZERO;
        BigDecimal employerContribution = BigDecimal.ZERO;

        for (final BigDecimal gross : grossAmounts) {
            final BigDecimal safeGross = gross == null ? BigDecimal.ZERO : gross.max(BigDecimal.ZERO);
            final StatutoryBreakdown breakdown = safeBreakdown(normalizedTenant, normalizedCountry, safeGross);
            totalGross = totalGross.add(breakdown.grossMonthly());
            employeeContribution = employeeContribution.add(breakdown.employeeTotal());
            employerContribution = employerContribution.add(breakdown.employerTotal());
            for (final StatutoryComponentAmount component : breakdown.components()) {
                componentTotals.merge(component.componentCode(), component.amount(), BigDecimal::add);
                employerFlags.putIfAbsent(component.componentCode(), component.employerBorne());
            }
        }

        final List<PayrollStatutoryFiling.ComponentTotal> componentLines = new ArrayList<>();
        for (final Map.Entry<String, BigDecimal> entry : componentTotals.entrySet()) {
            componentLines.add(new PayrollStatutoryFiling.ComponentTotal(
                entry.getKey(),
                employerFlags.getOrDefault(entry.getKey(), false),
                scale(entry.getValue())
            ));
        }

        final PayrollStatutoryFilingEntity entity = new PayrollStatutoryFilingEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantCode(normalizedTenant);
        entity.setCountryCode(normalizedCountry);
        entity.setPeriod(period);
        entity.setFilingType(filingType);
        entity.setReferenceNumber(buildReference(filingType, normalizedCountry, period));
        entity.setStatus(STATUS_GENERATED);
        entity.setEmployeeCount(grossAmounts.size());
        entity.setTotalGross(scale(totalGross));
        entity.setEmployeeContribution(scale(employeeContribution));
        entity.setEmployerContribution(scale(employerContribution));
        entity.setTotalContribution(scale(employeeContribution.add(employerContribution)));
        entity.setComponentTotalsJson(writeJson(componentLines));
        entity.setGeneratedAt(Instant.now());
        final PayrollStatutoryFilingEntity saved = filingRepository.save(entity);

        auditEventService.record(AuditEventRecord
            .of(normalizedTenant, AUDIT_MODULE, "PAYROLL_STATUTORY_FILING_GENERATED", "SUCCESS")
            .withActor(actorEmail, null)
            .withTarget("PAYROLL_STATUTORY_FILING", saved.getId()));

        return toModel(saved, componentLines);
    }

    /**
     * Lists statutory filings for a tenant and country, newest period first.
     *
     * @param tenantCode  owning tenant.
     * @param countryCode statutory country.
     * @return filing artifacts.
     */
    @Transactional(readOnly = true)
    public List<PayrollStatutoryFiling> list(final String tenantCode, final String countryCode) {
        final String normalizedTenant = require(tenantCode, "Tenant code is required.");
        final String normalizedCountry = require(countryCode, "Country code is required.").toUpperCase(Locale.ROOT);
        return filingRepository
            .findAllByTenantCodeIgnoreCaseAndCountryCodeIgnoreCaseOrderByPeriodDescGeneratedAtDesc(normalizedTenant, normalizedCountry)
            .stream()
            .map((entity) -> toModel(entity, readComponentTotals(entity.getComponentTotalsJson())))
            .toList();
    }

    /**
     * Fetches a statutory filing by id within the tenant boundary.
     */
    @Transactional(readOnly = true)
    public PayrollStatutoryFiling findById(final String tenantCode, final String filingId) {
        final PayrollStatutoryFilingEntity entity = loadEntity(tenantCode, filingId);
        return toModel(entity, readComponentTotals(entity.getComponentTotalsJson()));
    }

    /**
     * Marks a generated filing as submitted to the statutory authority.
     */
    @Transactional
    public PayrollStatutoryFiling submit(
        final String tenantCode,
        final String filingId,
        final String submissionReference,
        final String actorEmail
    ) {
        final PayrollStatutoryFilingEntity entity = loadEntity(tenantCode, filingId);
        if (!STATUS_GENERATED.equalsIgnoreCase(entity.getStatus())) {
            throw new PayrollBusinessException("Only generated filings can be submitted. Current status: " + entity.getStatus());
        }
        entity.setStatus(STATUS_SUBMITTED);
        if (submissionReference != null && !submissionReference.isBlank()) {
            entity.setReferenceNumber(entity.getReferenceNumber() + "|SUB:" + submissionReference.trim());
        }
        final PayrollStatutoryFilingEntity saved = filingRepository.save(entity);
        auditEventService.record(AuditEventRecord
            .of(require(tenantCode, "Tenant code is required."), AUDIT_MODULE, "PAYROLL_STATUTORY_FILING_SUBMITTED", "SUCCESS")
            .withActor(actorEmail, null)
            .withTarget("PAYROLL_STATUTORY_FILING", saved.getId()));
        return toModel(saved, readComponentTotals(saved.getComponentTotalsJson()));
    }

    /**
     * Locks a submitted filing so it can no longer be mutated.
     */
    @Transactional
    public PayrollStatutoryFiling lock(final String tenantCode, final String filingId, final String actorEmail) {
        final PayrollStatutoryFilingEntity entity = loadEntity(tenantCode, filingId);
        if (!STATUS_SUBMITTED.equalsIgnoreCase(entity.getStatus())) {
            throw new PayrollBusinessException("Only submitted filings can be locked. Current status: " + entity.getStatus());
        }
        entity.setStatus(STATUS_LOCKED);
        final PayrollStatutoryFilingEntity saved = filingRepository.save(entity);
        auditEventService.record(AuditEventRecord
            .of(require(tenantCode, "Tenant code is required."), AUDIT_MODULE, "PAYROLL_STATUTORY_FILING_LOCKED", "SUCCESS")
            .withActor(actorEmail, null)
            .withTarget("PAYROLL_STATUTORY_FILING", saved.getId()));
        return toModel(saved, readComponentTotals(saved.getComponentTotalsJson()));
    }

    /**
     * Builds a country-aware statutory export payload suitable for downstream
     * filing systems (PF ECR, US Form 941, UK RTI FPS, or generic JSON).
     */
    @Transactional(readOnly = true)
    public Map<String, Object> buildExportPayload(final String tenantCode, final String filingId) {
        final PayrollStatutoryFilingEntity entity = loadEntity(tenantCode, filingId);
        final List<PayrollStatutoryFiling.ComponentTotal> components = readComponentTotals(entity.getComponentTotalsJson());
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("exportVersion", "1.0");
        payload.put("filingId", entity.getId());
        payload.put("tenantCode", entity.getTenantCode());
        payload.put("countryCode", entity.getCountryCode());
        payload.put("period", entity.getPeriod());
        payload.put("filingType", entity.getFilingType());
        payload.put("referenceNumber", entity.getReferenceNumber());
        payload.put("status", entity.getStatus());
        payload.put("employeeCount", entity.getEmployeeCount());
        payload.put("totalGross", entity.getTotalGross());
        payload.put("employeeContribution", entity.getEmployeeContribution());
        payload.put("employerContribution", entity.getEmployerContribution());
        payload.put("totalContribution", entity.getTotalContribution());
        payload.put("componentTotals", components);
        payload.put("generatedAt", entity.getGeneratedAt());

        switch (entity.getCountryCode().toUpperCase(Locale.ROOT)) {
            case "IN" -> payload.put("submissionFormat", "PF_ESI_PT_ECR_JSON");
            case "US" -> payload.put("submissionFormat", "FORM_941_JSON");
            case "GB" -> payload.put("submissionFormat", "RTI_FPS_JSON");
            case "DE" -> payload.put("submissionFormat", "DE_SOCIAL_JSON");
            case "AE", "SG" -> payload.put("submissionFormat", "GCC_STATUTORY_JSON");
            default -> payload.put("submissionFormat", "GENERIC_STATUTORY_JSON");
        }
        return payload;
    }

    /**
     * Builds a minimal XML export document for downstream statutory filing adapters.
     */
    @Transactional(readOnly = true)
    public String buildExportXml(final String tenantCode, final String filingId) {
        final Map<String, Object> payload = buildExportPayload(tenantCode, filingId);
        final StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<StatutoryFilingExport version=\"")
            .append(escapeXml(String.valueOf(payload.get("exportVersion"))))
            .append("\">\n");
        appendXmlElement(xml, "filingId", payload.get("filingId"));
        appendXmlElement(xml, "tenantCode", payload.get("tenantCode"));
        appendXmlElement(xml, "countryCode", payload.get("countryCode"));
        appendXmlElement(xml, "period", payload.get("period"));
        appendXmlElement(xml, "filingType", payload.get("filingType"));
        appendXmlElement(xml, "referenceNumber", payload.get("referenceNumber"));
        appendXmlElement(xml, "status", payload.get("status"));
        appendXmlElement(xml, "employeeCount", payload.get("employeeCount"));
        appendXmlElement(xml, "totalGross", payload.get("totalGross"));
        appendXmlElement(xml, "employeeContribution", payload.get("employeeContribution"));
        appendXmlElement(xml, "employerContribution", payload.get("employerContribution"));
        appendXmlElement(xml, "totalContribution", payload.get("totalContribution"));
        appendXmlElement(xml, "submissionFormat", payload.get("submissionFormat"));
        appendXmlElement(xml, "generatedAt", payload.get("generatedAt"));
        xml.append("  <componentTotals>\n");
        @SuppressWarnings("unchecked")
        final List<PayrollStatutoryFiling.ComponentTotal> components =
            (List<PayrollStatutoryFiling.ComponentTotal>) payload.get("componentTotals");
        if (components != null) {
            for (PayrollStatutoryFiling.ComponentTotal component : components) {
                xml.append("    <component>\n");
                appendXmlElement(xml, "componentCode", component.componentCode(), 6);
                appendXmlElement(xml, "employerBorne", component.employerBorne(), 6);
                appendXmlElement(xml, "amount", component.amount(), 6);
                xml.append("    </component>\n");
            }
        }
        xml.append("  </componentTotals>\n");
        xml.append("</StatutoryFilingExport>");
        return xml.toString();
    }

    private void appendXmlElement(final StringBuilder xml, final String name, final Object value) {
        appendXmlElement(xml, name, value, 2);
    }

    private void appendXmlElement(final StringBuilder xml, final String name, final Object value, final int indent) {
        xml.append(" ".repeat(indent))
            .append('<').append(name).append('>');
        if (value != null) {
            xml.append(escapeXml(String.valueOf(value)));
        }
        xml.append("</").append(name).append(">\n");
    }

    private String escapeXml(final String raw) {
        return raw.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }

    private PayrollStatutoryFilingEntity loadEntity(final String tenantCode, final String filingId) {
        return filingRepository.findByIdAndTenantCodeIgnoreCase(
                require(filingId, "Filing id is required."),
                require(tenantCode, "Tenant code is required."))
            .orElseThrow(() -> new PayrollBusinessException("Statutory filing not found for id: " + filingId));
    }

    private PayrollStatutoryFiling toModel(
        final PayrollStatutoryFilingEntity entity,
        final List<PayrollStatutoryFiling.ComponentTotal> componentLines
    ) {
        return new PayrollStatutoryFiling(
            entity.getId(),
            entity.getTenantCode(),
            entity.getCountryCode(),
            entity.getPeriod(),
            entity.getFilingType(),
            entity.getReferenceNumber(),
            entity.getStatus(),
            entity.getEmployeeCount(),
            entity.getTotalGross(),
            entity.getEmployeeContribution(),
            entity.getEmployerContribution(),
            entity.getTotalContribution(),
            componentLines,
            entity.getGeneratedAt()
        );
    }

    private String buildReference(final String filingType, final String countryCode, final String period) {
        final String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        return filingType + "-" + countryCode + "-" + period.replace("-", "") + "-" + suffix;
    }

    private String writeJson(final List<PayrollStatutoryFiling.ComponentTotal> componentLines) {
        try {
            return objectMapper.writeValueAsString(componentLines);
        } catch (JsonProcessingException ex) {
            throw new NexraValidationException("Failed to serialize statutory component totals.");
        }
    }

    private List<PayrollStatutoryFiling.ComponentTotal> readComponentTotals(final String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory()
                .constructCollectionType(List.class, PayrollStatutoryFiling.ComponentTotal.class));
        } catch (JsonProcessingException ex) {
            return List.of();
        }
    }

    private StatutoryBreakdown safeBreakdown(final String tenant, final String country, final BigDecimal gross) {
        try {
            return statutoryService.calculateBreakdown(tenant, country, gross);
        } catch (NexraValidationException ex) {
            throw new PayrollBusinessException(ex.getMessage());
        }
    }

    private BigDecimal scale(final BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String require(final String value, final String message) {
        if (value == null || value.isBlank()) {
            throw new PayrollBusinessException(message);
        }
        return value.trim();
    }
}
