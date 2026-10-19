package org.Money_Making_Machine.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The data sent back to the client after creating, fetching, or updating an invoice.
 *
 * This is the full picture of an invoice from the API's perspective.
 * All calculated fields (subtotal, total) are included here because
 * the service computed them — the client can trust these values.
 */
@Data    // Lombok: generates getters, setters, equals, hashCode, toString
@Builder // Lombok: lets us use InvoiceResponse.builder().id(1L).build()
public class InvoiceResponse {

    private Long id;
    private String customerName;
    private String customerEmail;

    /** The calculated line items, each with their individual totals. */
    private List<InvoiceItemResponse> items;

    /** Sum of all item totals. Calculated by the service. */
    private BigDecimal subtotal;

    /** Tax amount provided by the client. */
    private BigDecimal tax;

    /** Discount amount provided by the client. */
    private BigDecimal discount;

    /** Final amount: subtotal + tax - discount. Calculated by the service. */
    private BigDecimal total;

    /** When this invoice was first created. Set automatically on save. */
    private LocalDateTime createdAt;
}
