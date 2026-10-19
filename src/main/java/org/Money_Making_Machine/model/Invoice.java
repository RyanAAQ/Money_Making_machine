package org.Money_Making_Machine.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a full invoice sent to a customer.
 * This is the main entity of the application.
 *
 * @Entity tells JPA this class maps to a database table.
 * @Table  names the table "invoices" in the database.
 */
@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "items") // prevents lazy-load traversal in equals/hashCode
@ToString(exclude = "items")          // prevents lazy-load traversal in toString
public class Invoice {

    /**
     * Primary key. PostgreSQL will auto-increment this.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the customer receiving the invoice.
     */
    @NotBlank(message = "Customer name cannot be blank")
    @Column(nullable = false)
    private String customerName;

    /**
     * The email address of the customer.
     * @Email validates the format (must contain @ and a domain).
     */
    @Email(message = "Customer email must be a valid email address")
    @NotBlank(message = "Customer email cannot be blank")
    @Column(nullable = false)
    private String customerEmail;

    /**
     * The list of items on this invoice.
     *
     * @OneToMany means one invoice has many items.
     * mappedBy = "invoice" tells JPA that the InvoiceItem.invoice field owns the relationship.
     * cascade = ALL means when we save/delete an invoice, its items are automatically saved/deleted too.
     * orphanRemoval = true means if we remove an item from this list, it is deleted from the DB.
     */
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // Required so Lombok's @Builder initializes this list instead of leaving it null
    private List<InvoiceItem> items = new ArrayList<>();

    /**
     * Sum of all item totals (quantity × unitPrice for each item).
     * Calculated by the service — never trusted from the client.
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    /**
     * Tax amount added to the invoice (a flat amount, not a percentage).
     * Must be zero or positive.
     */
    @DecimalMin(value = "0.00", message = "Tax cannot be negative")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal tax;

    /**
     * Discount amount subtracted from the invoice (a flat amount, not a percentage).
     * Must be zero or positive.
     */
    @DecimalMin(value = "0.00", message = "Discount cannot be negative")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal discount;

    /**
     * Final amount owed: subtotal + tax - discount.
     * Calculated by the service — never trusted from the client.
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    /**
     * When the invoice was created. Set automatically before saving to the DB.
     * @PrePersist runs this method automatically the first time the entity is saved.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
