# Invoices

The Invoices resource lets you create professional invoices, track payments against them, and manage the invoice lifecycle from draft to paid. This is useful for businesses that need to bill customers before collecting payment -- freelancers, service providers, and B2B operations.

All invoice methods are accessed via `malipopay.invoices()`.

## Creating an Invoice

```java
import co.tz.malipopay.Malipopay;
import co.tz.malipopay.exceptions.MalipopayException;

import java.util.*;

Malipopay malipopay = new Malipopay("your-api-key");

try {
    Map<String, Object> item1 = new HashMap<>();
    item1.put("description", "Web Development - Landing Page");
    item1.put("quantity", 1);
    item1.put("unitPrice", 500000);

    Map<String, Object> item2 = new HashMap<>();
    item2.put("description", "Hosting Setup (1 year)");
    item2.put("quantity", 1);
    item2.put("unitPrice", 150000);

    Map<String, Object> params = new HashMap<>();
    params.put("customerName", "Amina Juma");
    params.put("customerPhone", "255712345678");
    params.put("customerEmail", "amina@example.com");
    params.put("dueDate", "2024-04-30");
    params.put("items", List.of(item1, item2));
    params.put("tax", 18);   // VAT percentage
    params.put("notes", "Payment due within 30 days. Thank you for your business!");

    Map<String, Object> invoice = malipopay.invoices().create(params);

    System.out.println("Invoice created: " + invoice.get("invoiceNo"));
    System.out.println("Total: TZS " + invoice.get("total"));
} catch (MalipopayException e) {
    System.err.println("Error: " + e.getMessage());
}
```

### Line Items

Each item in the `items` list should include:

| Field         | Type    | Description                          |
|---------------|---------|--------------------------------------|
| `description` | String  | What the charge is for               |
| `quantity`    | int     | Number of units                      |
| `unitPrice`   | int    | Price per unit in TZS               |

The server calculates subtotal, tax, and total automatically.

### Tax

The `tax` field is a percentage. Pass `18` for 18% VAT (Tanzania's standard rate). Pass `0` or omit it for tax-exempt invoices.

## Retrieving an Invoice

### By ID

```java
Map<String, Object> invoice = malipopay.invoices().get("64a1b2c3d4e5f6a7b8c9d0e1");
```

### By Invoice Number

```java
Map<String, Object> invoice = malipopay.invoices().getByNumber("INV-2024-0042");
```

## Listing Invoices

```java
// All invoices
Map<String, Object> invoices = malipopay.invoices().list();

// Paginated
Map<String, Object> filters = new HashMap<>();
filters.put("page", 1);
filters.put("limit", 20);

Map<String, Object> page = malipopay.invoices().list(filters);

List<Map<String, Object>> items = (List<Map<String, Object>>) page.get("items");
for (Map<String, Object> inv : items) {
    System.out.println(inv.get("invoiceNo") + " - " + inv.get("customerName") + " - TZS " + inv.get("total"));
}
```

## Updating an Invoice

Update an invoice that hasn't been finalized yet:

```java
Map<String, Object> item1 = new HashMap<>();
item1.put("description", "Web Development - Landing Page");
item1.put("quantity", 1);
item1.put("unitPrice", 500000);

Map<String, Object> item2 = new HashMap<>();
item2.put("description", "Hosting Setup (1 year)");
item2.put("quantity", 1);
item2.put("unitPrice", 150000);

Map<String, Object> item3 = new HashMap<>();
item3.put("description", "Domain Registration (.co.tz)");
item3.put("quantity", 1);
item3.put("unitPrice", 50000);

Map<String, Object> updates = new HashMap<>();
updates.put("dueDate", "2024-05-15");
updates.put("notes", "Extended payment deadline per customer request.");
updates.put("items", List.of(item1, item2, item3));

malipopay.invoices().update("64a1b2c3d4e5f6a7b8c9d0e1", updates);
```

## Recording a Payment

When a customer pays (partially or fully) against an invoice:

```java
Map<String, Object> params = new HashMap<>();
params.put("invoiceId", "64a1b2c3d4e5f6a7b8c9d0e1");
params.put("amount", 350000);
params.put("paymentMethod", "mobile_money");
params.put("reference", "PAY-2024-00123");
params.put("notes", "Partial payment received via M-Pesa");

malipopay.invoices().recordPayment(params);
```

This is useful when:

- A customer pays in installments.
- You receive payment through a channel outside Malipopay.
- You want to track cash or bank transfer payments against the same invoice.

## Approving a Draft Invoice

Invoices start as drafts for internal review. Once approved, the invoice becomes final:

```java
Map<String, Object> params = new HashMap<>();
params.put("invoiceId", "64a1b2c3d4e5f6a7b8c9d0e1");

malipopay.invoices().approveDraft(params);
```

### Draft Workflow

1. **Create** the invoice -- it starts as a draft.
2. **Review** internally -- check amounts, line items, customer details.
3. **Approve** the draft -- the invoice is now active.
4. **Collect payment** or wait for the customer to pay via a payment link.
5. **Record payment** once funds are received.

## Getting the Next Invoice Number

Preview the next auto-generated invoice number:

```java
Map<String, Object> next = malipopay.invoices().nextInvoiceNo();
System.out.println("Next invoice will be: " + next.get("invoiceNo"));
```

## Complete Example: Invoice-to-Payment Flow

```java
import co.tz.malipopay.Malipopay;
import co.tz.malipopay.exceptions.MalipopayException;

import java.util.*;

Malipopay malipopay = new Malipopay("your-api-key");

// 1. Create the invoice
Map<String, Object> consultingItem = new HashMap<>();
consultingItem.put("description", "Consulting Services (40 hrs)");
consultingItem.put("quantity", 40);
consultingItem.put("unitPrice", 50000);

Map<String, Object> travelItem = new HashMap<>();
travelItem.put("description", "Travel Expenses");
travelItem.put("quantity", 1);
travelItem.put("unitPrice", 200000);

Map<String, Object> invoiceParams = new HashMap<>();
invoiceParams.put("customerName", "Baraka Enterprises Ltd");
invoiceParams.put("customerPhone", "255754987654");
invoiceParams.put("customerEmail", "accounts@baraka.co.tz");
invoiceParams.put("dueDate", "2024-04-15");
invoiceParams.put("items", List.of(consultingItem, travelItem));
invoiceParams.put("tax", 18);

Map<String, Object> invoice = malipopay.invoices().create(invoiceParams);
String invoiceId = (String) invoice.get("_id");
System.out.println("Created invoice: " + invoice.get("invoiceNo"));

// 2. Approve the draft
Map<String, Object> approveParams = new HashMap<>();
approveParams.put("invoiceId", invoiceId);
malipopay.invoices().approveDraft(approveParams);

// 3. Collect payment via mobile money
try {
    Map<String, Object> paymentParams = new HashMap<>();
    paymentParams.put("amount", invoice.get("total"));
    paymentParams.put("phone", "255754987654");
    paymentParams.put("provider", "Vodacom");

    Map<String, Object> payment = malipopay.payments().collect(paymentParams);

    // 4. Record the payment against the invoice
    Map<String, Object> recordParams = new HashMap<>();
    recordParams.put("invoiceId", invoiceId);
    recordParams.put("amount", invoice.get("total"));
    recordParams.put("paymentMethod", "mobile_money");
    recordParams.put("reference", payment.get("reference"));

    malipopay.invoices().recordPayment(recordParams);

    System.out.println("Invoice paid in full!");
} catch (MalipopayException e) {
    System.err.println("Payment collection failed: " + e.getMessage());
}
```
