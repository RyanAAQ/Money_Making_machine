# Money Making Machine (Quick Invoice API)

A REST API for creating and managing invoices. You send it a customer and a list of items, and it works out the subtotal and total, saves everything in PostgreSQL, and lets you read, update or delete invoices later.

Built with **Spring Boot**, **Spring Data JPA (Hibernate)** and **PostgreSQL**.

## What it does

An invoice has a customer name, a customer email, one or more line items, a tax amount and a discount amount. When you create or update an invoice:

1. The API checks the request is valid (see Validation below).
2. It works out each line total as `quantity × unit price`.
3. It adds those up to get the **subtotal**.
4. It calculates `total = subtotal + tax − discount`.
5. It saves the invoice and its items and returns the full invoice, including the calculated figures and a creation timestamp.

The subtotal and total are always calculated by the server, never taken from the client, so the numbers on an invoice can't be wrong because of a bad request.

### Validation

- Customer name is required
- Customer email is required and must be a valid email address
- An invoice must contain at least one item
- Each item needs a name, a quantity of at least 1, and a unit price that is not negative
- Tax and discount are required and cannot be negative (send `0.00` if none applies)

Invalid requests get a `400` response that lists exactly which fields failed and why.

## API

Base path: `/api/invoices` (the server runs on port `8080`)

| Method | Path | What it does |
|---|---|---|
| POST | `/api/invoices` | Create an invoice |
| GET | `/api/invoices` | List all invoices |
| GET | `/api/invoices/{id}` | Get one invoice |
| PUT | `/api/invoices/{id}` | Update an invoice (totals are recalculated) |
| DELETE | `/api/invoices/{id}` | Delete an invoice and its items |

`GET /` returns a welcome message, handy as a quick check that the app is running.

### Example request

```json
POST /api/invoices
{
  "customerName": "Ada Okafor",
  "customerEmail": "ada@example.com",
  "items": [
    { "name": "Logo design", "quantity": 1, "unitPrice": 50000.00 },
    { "name": "Business cards", "quantity": 4, "unitPrice": 2500.00 }
  ],
  "tax": 4500.00,
  "discount": 2000.00
}
```

Here the subtotal is `60000.00` and the total is `62500.00`.

### Example error

```json
{
  "timestamp": "2026-10-21T10:15:30",
  "status": 400,
  "error": "Validation failed",
  "fieldErrors": {
    "customerEmail": "Customer email must be a valid email address"
  }
}
```

A request for an invoice that doesn't exist returns `404`, and any unexpected failure returns a clean `500` message instead of a stack trace.

## How it is built

```
src/main/java/org/Money_Making_Machine/
├── controller/    InvoiceController: the REST endpoints
├── service/       InvoiceService: calculations and business logic
├── model/         Invoice and InvoiceItem (JPA entities, one invoice to many items)
├── repository/    InvoiceRepository (Spring Data JPA)
├── dto/           Request and response objects, with validation rules
└── exception/     InvoiceNotFoundException and a global exception handler
```

- Money values use `BigDecimal` so there are no floating-point rounding errors.
- Invoice items belong to their invoice, so deleting an invoice removes its items too.
- Database details are read from environment variables, so no passwords are written in the code.

## Run it locally

**You need:** Java 26 (as set in `pom.xml`), Maven and PostgreSQL.

1. Create a PostgreSQL database (for example `quickinvoice`).
2. Set these environment variables:

   | Variable | Meaning |
   |---|---|
   | `DB_HOST` | Where PostgreSQL runs (usually `localhost`) |
   | `DB_PORT` | PostgreSQL port (usually `5432`) |
   | `DB_NAME` | Your database name |
   | `DB_USERNAME` | Your PostgreSQL username |
   | `DB_PASSWORD` | Your PostgreSQL password |

3. Start the app:
   ```bash
   mvn spring-boot:run
   ```

Hibernate creates and updates the tables for you on startup. You can then call the API with Postman or `curl`.

## Tech stack

Java, Spring Boot, Spring Web, Spring Data JPA / Hibernate, PostgreSQL, Jakarta Bean Validation, Lombok, Maven
