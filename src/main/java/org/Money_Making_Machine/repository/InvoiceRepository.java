package org.Money_Making_Machine.repository;

import org.Money_Making_Machine.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * The repository is the layer that talks directly to the database.
 *
 * By extending JpaRepository<Invoice, Long> we automatically get:
 *   - save(invoice)          → INSERT or UPDATE
 *   - findById(id)           → SELECT by primary key
 *   - findAll()              → SELECT all rows
 *   - deleteById(id)         → DELETE by primary key
 *   - existsById(id)         → check if a row exists
 *   ...and many more — all without writing any SQL.
 *
 * The two type parameters mean:
 *   Invoice → the entity class this repository manages
 *   Long    → the type of the primary key (our Invoice.id is a Long)
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    // No custom methods needed yet.
    // Spring Data JPA generates all the SQL we need from the method names above.
}
