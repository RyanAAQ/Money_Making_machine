package org.Money_Making_Machine.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.Money_Making_Machine.dto.InvoiceRequest;
import org.Money_Making_Machine.dto.InvoiceResponse;
import org.Money_Making_Machine.service.InvoiceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The controller is the front door of the API.
 * It receives HTTP requests, hands them to the service, and returns HTTP responses.
 *
 * The controller does NOT contain business logic — it only:
 *   1. Accepts the incoming request
 *   2. Calls the correct service method
 *   3. Returns the result with the correct HTTP status code
 *
 * @RestController  = @Controller + @ResponseBody (auto-converts return values to JSON)
 * @RequestMapping  sets the base URL for all endpoints in this class → /api/invoices
 * @RequiredArgsConstructor (Lombok) injects InvoiceService via constructor
 */
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    /**
     * POST /api/invoices
     *
     * Creates a new invoice.
     * @Valid triggers Jakarta validation on the InvoiceRequest body.
     * If validation fails, GlobalExceptionHandler catches it and returns 400.
     *
     * Returns: 201 Created + the saved invoice with calculated totals
     */
    @PostMapping
    public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody InvoiceRequest request) {
        InvoiceResponse response = invoiceService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/invoices
     *
     * Returns all invoices in the database.
     * Returns an empty list (not an error) if no invoices exist yet.
     *
     * Returns: 200 OK + list of invoices
     */
    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> getAllInvoices() {
        List<InvoiceResponse> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }

    /**
     * GET /api/invoices/{id}
     *
     * Returns a single invoice by its ID.
     * If the ID doesn't exist, GlobalExceptionHandler returns 404.
     *
     * Returns: 200 OK + the invoice, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable Long id) {
        InvoiceResponse response = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/invoices/{id}
     *
     * Replaces an existing invoice with new data.
     * All money fields are recalculated from the new items.
     * If the ID doesn't exist, GlobalExceptionHandler returns 404.
     *
     * Returns: 200 OK + the updated invoice, or 404 Not Found
     */
    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponse> updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceRequest request) {
        InvoiceResponse response = invoiceService.updateInvoice(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/invoices/{id}
     *
     * Deletes an invoice and all its items (cascade delete).
     * If the ID doesn't exist, GlobalExceptionHandler returns 404.
     *
     * Returns: 204 No Content (success with no body), or 404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }
}
