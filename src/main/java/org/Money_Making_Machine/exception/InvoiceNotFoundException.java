package org.Money_Making_Machine.exception;

/**
 * Thrown when a client requests an invoice by ID and no invoice with that ID exists.
 *
 * By extending RuntimeException we don't have to declare it on every method signature
 * with "throws InvoiceNotFoundException" — Spring will catch it via the GlobalExceptionHandler.
 */
public class InvoiceNotFoundException extends RuntimeException {

    public InvoiceNotFoundException(Long id) {
        super("Invoice not found with id: " + id);
    }
}
