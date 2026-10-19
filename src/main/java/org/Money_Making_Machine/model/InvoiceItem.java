package org.Money_Making_Machine.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Represents a single line item on an invoice.
 * For example: "Website Design x 1 @ $500.00"
 *
 * @Entity  tells JPA this class maps to a database table.
 * @Table   names the table "invoice_items" in the database.
 */
@Entity
@Table(name = "invoice_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "invoice") // prevents circular Invoice → InvoiceItem → Invoice toString loop
public class InvoiceItem {

    /**
     * Primary key. IDENTITY strategy means PostgreSQL auto-increments this value.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name/description of the service or product.
     * Example: "Website Design", "Logo Package"
     */
    @NotBlank(message = "Item name cannot be blank")
    @Column(nullable = false)
    private String name;

    /**
     * How many units of this item. Must be at least 1.
     */
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(nullable = false)
    private int quantity;

    /**
     * The price per single unit.
     * BigDecimal is used instead of double because it handles money precisely.
     * (double can have rounding errors like 0.1 + 0.2 = 0.30000000000000004)
     */
    @DecimalMin(value = "0.00", message = "Unit price cannot be negative")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    /**
     * The back-reference to the parent invoice.
     * @ManyToOne means many items can belong to one invoice.
     * @JoinColumn creates a foreign key column "invoice_id" in this table.
     * FetchType.LAZY means the invoice is only loaded from the DB when you actually need it.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;
}
