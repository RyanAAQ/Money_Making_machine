package org.Money_Making_Machine.service;

import lombok.RequiredArgsConstructor;
import org.Money_Making_Machine.dto.*;
import org.Money_Making_Machine.exception.InvoiceNotFoundException;
import org.Money_Making_Machine.model.Invoice;
import org.Money_Making_Machine.model.InvoiceItem;
import org.Money_Making_Machine.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * The service layer contains all business logic.
 * The controller handles HTTP — the service handles what actually happens.
 *
 * Rules enforced here:
 *  - subtotal is always calculated from the items (never trusted from the client)
 *  - total is always calculated as subtotal + tax - discount
 *  - every invoice must have its items linked back to it before saving
 *
 * @Service marks this as a Spring-managed component.
 * @RequiredArgsConstructor (Lombok) generates a constructor for all 'final' fields,
 * which is how Spring injects the InvoiceRepository automatically.
 */
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    /**
     * Creates a new invoice, calculates all money fields, and saves it to the database.
     *
     * @param request The data sent by the client (customerName, email, items, tax, discount)
     * @return An InvoiceResponse containing the saved invoice with all calculated fields
     */
    @Transactional
    public InvoiceResponse createInvoice(InvoiceRequest request) {
        // Use the builder so @Builder.Default initializes items to an empty ArrayList
        Invoice invoice = Invoice.builder().build();
        buildInvoiceFromRequest(request, invoice);
        Invoice saved = invoiceRepository.save(invoice);
        return mapToResponse(saved);
    }

    // -------------------------------------------------------------------------
    // READ ALL
    // -------------------------------------------------------------------------

    /**
     * Retrieves every invoice from the database.
     *
     * @return A list of InvoiceResponse objects (empty list if none exist)
     */
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // -------------------------------------------------------------------------
    // READ ONE
    // -------------------------------------------------------------------------

    /**
     * Retrieves a single invoice by its ID.
     *
     * @param id The invoice ID from the URL path
     * @return The matching InvoiceResponse
     * @throws InvoiceNotFoundException if no invoice with that ID exists
     */
    public InvoiceResponse getInvoiceById(Long id) {
        Invoice invoice = findInvoiceOrThrow(id);
        return mapToResponse(invoice);
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    /**
     * Replaces an existing invoice's data with the new data from the request.
     * All money fields are recalculated from scratch.
     *
     * @param id      The ID of the invoice to update
     * @param request The new data from the client
     * @return The updated InvoiceResponse
     * @throws InvoiceNotFoundException if no invoice with that ID exists
     */
    @Transactional
    public InvoiceResponse updateInvoice(Long id, InvoiceRequest request) {
        Invoice existing = findInvoiceOrThrow(id);

        // Clear the old items — orphanRemoval = true on Invoice.items means
        // the old InvoiceItem rows will be deleted from the DB automatically.
        existing.getItems().clear();

        buildInvoiceFromRequest(request, existing);
        Invoice saved = invoiceRepository.save(existing);
        return mapToResponse(saved);
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    /**
     * Deletes an invoice by its ID.
     * Because of cascade = ALL on Invoice.items, the child InvoiceItem rows
     * are also deleted automatically.
     *
     * @param id The ID of the invoice to delete
     * @throws InvoiceNotFoundException if no invoice with that ID exists
     */
    @Transactional
    public void deleteInvoice(Long id) {
        findInvoiceOrThrow(id); // verify it exists before trying to delete
        invoiceRepository.deleteById(id);
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * Populates an Invoice entity from an InvoiceRequest DTO.
     * This is shared by both createInvoice and updateInvoice.
     *
     * Money calculation steps:
     *   1. For each item: itemTotal = quantity × unitPrice
     *   2. subtotal = sum of all itemTotals
     *   3. total = subtotal + tax - discount
     */
    private Invoice buildInvoiceFromRequest(InvoiceRequest request, Invoice invoice) {
        invoice.setCustomerName(request.getCustomerName());
        invoice.setCustomerEmail(request.getCustomerEmail());
        invoice.setTax(request.getTax());
        invoice.setDiscount(request.getDiscount());

        // Convert each InvoiceItemRequest into an InvoiceItem entity.
        // We also set item.setInvoice(invoice) to establish the relationship —
        // without this, the foreign key "invoice_id" would be null in the DB.
        List<InvoiceItem> items = request.getItems().stream()
                .map(itemRequest -> {
                    InvoiceItem item = new InvoiceItem();
                    item.setName(itemRequest.getName());
                    item.setQuantity(itemRequest.getQuantity());
                    item.setUnitPrice(itemRequest.getUnitPrice());
                    item.setInvoice(invoice); // link back to parent invoice
                    return item;
                })
                .toList();

        invoice.getItems().addAll(items);

        // Step 1 & 2: calculate subtotal = sum of (quantity × unitPrice) for all items
        BigDecimal subtotal = items.stream()
                .map(item -> item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.setSubtotal(subtotal);

        // Step 3: total = subtotal + tax - discount
        BigDecimal total = subtotal
                .add(request.getTax())
                .subtract(request.getDiscount());

        invoice.setTotal(total);

        return invoice;
    }

    /**
     * Fetches an invoice from the repository, or throws InvoiceNotFoundException.
     * Centralizes the "find or throw" pattern so we don't repeat it everywhere.
     */
    private Invoice findInvoiceOrThrow(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
    }

    /**
     * Converts an Invoice entity into an InvoiceResponse DTO.
     *
     * This controls exactly what data the API sends back to the client.
     * We never return the raw entity — that would expose internal JPA details.
     */
    private InvoiceResponse mapToResponse(Invoice invoice) {
        // Map each InvoiceItem entity to an InvoiceItemResponse DTO
        List<InvoiceItemResponse> itemResponses = invoice.getItems().stream()
                .map(item -> InvoiceItemResponse.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        // recalculate itemTotal here for display convenience
                        .itemTotal(item.getUnitPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .customerName(invoice.getCustomerName())
                .customerEmail(invoice.getCustomerEmail())
                .items(itemResponses)
                .subtotal(invoice.getSubtotal())
                .tax(invoice.getTax())
                .discount(invoice.getDiscount())
                .total(invoice.getTotal())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
}
