package org.Money_Making_Machine.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * The data sent back to the client for a single line item.
 *
 * This mirrors the database entity but is a clean, controlled shape
 * for the API response. We include the calculated itemTotal here
 * so the client can display it without doing math.
 */
@Data    // Lombok: generates getters, setters, equals, hashCode, toString
@Builder // Lombok: lets us use InvoiceItemResponse.builder().name("X").build()
public class InvoiceItemResponse {

    private Long id;
    private String name;
    private int quantity;
    private BigDecimal unitPrice;

    /**
     * quantity × unitPrice — calculated by the service, shown here for convenience.
     */
    private BigDecimal itemTotal;
}
