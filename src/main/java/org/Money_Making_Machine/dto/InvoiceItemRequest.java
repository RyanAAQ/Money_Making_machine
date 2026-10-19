package org.Money_Making_Machine.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * The data a client sends for a single line item when creating or updating an invoice.
 *
 * This is NOT the database entity — it is just the shape of the incoming JSON.
 * Example JSON:
 * {
 *   "name": "Website Design",
 *   "quantity": 1,
 *   "unitPrice": 500.00
 * }
 */
@Data // Lombok: generates getters, setters, equals, hashCode, toString
public class InvoiceItemRequest {

    /**
     * Name or description of the service/product.
     * @NotBlank means it cannot be null, empty, or just whitespace.
     */
    @NotBlank(message = "Item name cannot be blank")
    private String name;

    /**
     * How many units. Must be at least 1.
     * @Min ensures the value is 1 or greater.
     */
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    /**
     * Price per unit. Cannot be null and cannot be negative.
     * @NotNull is needed because BigDecimal is an object (not a primitive),
     * so it can be null if the client forgets to send it.
     */
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.00", message = "Unit price cannot be negative")
    private BigDecimal unitPrice;
}
