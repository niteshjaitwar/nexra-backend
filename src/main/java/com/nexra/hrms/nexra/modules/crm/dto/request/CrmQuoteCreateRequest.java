package com.nexra.hrms.nexra.modules.crm.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request to create a sales quote with one or more line items.
 */
public record CrmQuoteCreateRequest(
    @NotBlank @Size(max = 240) String title,
    @Size(max = 4) String currency,
    @Size(max = 36) String dealId,
    @Size(max = 36) String accountId,
    @Size(max = 36) String contactId,
    @NotBlank @Size(max = 36) String ownerUserId,
    LocalDate validUntil,
    @NotEmpty @Valid List<LineItem> lineItems
) {

    /**
     * A line item on the quote.
     */
    public record LineItem(
        @NotBlank @Size(max = 240) String productName,
        @NotNull @Positive BigDecimal quantity,
        @NotNull @PositiveOrZero BigDecimal unitPrice,
        @PositiveOrZero @DecimalMax("100.0") BigDecimal discountPercent,
        @PositiveOrZero @DecimalMax("100.0") BigDecimal taxPercent
    ) {
    }
}
