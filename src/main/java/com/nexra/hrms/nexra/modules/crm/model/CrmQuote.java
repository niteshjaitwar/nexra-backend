package com.nexra.hrms.nexra.modules.crm.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * API representation of a sales quote and its line items.
 */
public record CrmQuote(
    String id,
    String tenantCode,
    String quoteNumber,
    String title,
    String status,
    String currency,
    String dealId,
    String accountId,
    String contactId,
    String ownerUserId,
    BigDecimal subtotal,
    BigDecimal discountTotal,
    BigDecimal taxTotal,
    BigDecimal grandTotal,
    LocalDate validUntil,
    List<LineItem> lineItems
) {

    /**
     * A single quote line item with its computed line total.
     */
    public record LineItem(
        String id,
        int lineNo,
        String productName,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal discountPercent,
        BigDecimal taxPercent,
        BigDecimal lineTotal
    ) {
    }
}
