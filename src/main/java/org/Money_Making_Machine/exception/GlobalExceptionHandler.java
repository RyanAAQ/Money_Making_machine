package org.Money_Making_Machine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Catches exceptions thrown anywhere in the application and converts them into
 * clean, consistent JSON error responses.
 *
 * Without this, Spring would expose raw stack traces or confusing HTML error pages.
 * With this, every error looks like:
 * {
 *   "timestamp": "2025-01-15T10:30:00",
 *   "status": 404,
 *   "error": "Invoice not found with id: 99"
 * }
 *
 * @RestControllerAdvice means this class watches all @RestController classes
 * and intercepts any exceptions they throw.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles the case when an invoice ID is not found in the database.
     * Returns HTTP 404 Not Found.
     */
    @ExceptionHandler(InvoiceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleInvoiceNotFound(InvoiceNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handles validation failures — for example when a client sends:
     * - a blank customerName
     * - an invalid email
     * - an empty items list
     * - a negative unit price
     *
     * Spring triggers this automatically when @Valid fails on a request body.
     * Returns HTTP 400 Bad Request with a map of each field and its error message.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        // Collect all field-level validation errors into a map: { "fieldName": "error message" }
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation failed");
        body.put("fieldErrors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Catches any unexpected exception that we haven't handled specifically.
     * Returns HTTP 500 Internal Server Error with a safe, generic message.
     *
     * IMPORTANT: we intentionally do NOT expose ex.getMessage() here,
     * because that could leak internal database errors or stack trace details to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later."
        );
    }

    /**
     * Helper method to build a consistent error response body.
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", message);
        return ResponseEntity.status(status).body(body);
    }
}
