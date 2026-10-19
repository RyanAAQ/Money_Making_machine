package org.Money_Making_Machine.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * The data a client sends when creating or updating an invoice.
 *
 * Notice: there is NO subtotal or total field here.
 * The client is NOT trusted to calculate money — the service does that.
 *
 * Example JSON:
 * {
 *   "customerName": "John Doe",
 *   "customerEmail": "john@example.com",
 *   "items": [
 *     { "name": "Website Design", "quantity": 1, "unitPrice": 500.00 },
 *     { "name": "Logo Design",    "quantity": 2, "unitPrice": 100.00 }
 *   ],
 *   "tax": 50.00,
 *   "discount": 20.00
 * }
 */
@Data // Lombok: generates getters, setters, equals, hashCode, toString
public class InvoiceRequest {

    /**
     * Full name of the customer receiving this invoice.
     */
    @NotBlank(message = "Customer name cannot be blank")
    private String customerName;

    /**
     * Email address of the customer.
     * @Email validates the format — must look like an email address.
     */
    @NotBlank(message = "Customer email cannot be blank")
    @Email(message = "Customer email must be a valid email address")
    private String customerEmail;

    /**
     * The list of line items on the invoice.
     * @NotEmpty means the list cannot be null or empty — every invoice needs at least one item.
     * @Valid tells Spring to also validate each InvoiceItemRequest inside the list.
     */
    @NotEmpty(message = "Invoice must contain at least one item")
    @Valid
    private List<InvoiceItemRequest> items;

    /**
     * Flat tax amount added to the invoice total.
     * Defaults to zero if not provided. Cannot be negative.
     */
    @NotNull(message = "Tax is required (use 0.00 if no tax applies)")
    @DecimalMin(value = "0.00", message = "Tax cannot be negative")
    private BigDecimal tax;

    /**
     * Flat discount amount subtracted from the invoice total.
     * Defaults to zero if not provided. Cannot be negative.
     */
    @NotNull(message = "Discount is required (use 0.00 if no discount applies)")
    @DecimalMin(value = "0.00", message = "Discount cannot be negative")
    private BigDecimal discount;
}
