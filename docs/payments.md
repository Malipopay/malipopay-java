# Payments

The Payments resource is at the heart of the MaliPoPay SDK. It lets you collect mobile money from customers, disburse funds to recipients, create payment links, verify transaction status, and more.

All payment methods are accessed via `malipopay.payments()`.

## Mobile Money Collection

Collection triggers a USSD push to the customer's phone. The customer confirms the payment on their handset, and MaliPoPay processes the transaction.

### Supported Providers

| Provider   | `provider` value | Network       |
|------------|------------------|---------------|
| M-Pesa     | `Vodacom`        | Vodacom       |
| Airtel Money | `Airtel`       | Airtel        |
| Mixx by Yas  | `Tigo`         | Tigo (MIC)    |
| Halopesa   | `Halotel`        | Halotel       |
| T-Pesa     | `TTCL`           | TTCL          |

### Basic Collection

```java
import co.tz.malipopay.MaliPoPay;
import co.tz.malipopay.exceptions.MaliPoPayException;

import java.util.HashMap;
import java.util.Map;

MaliPoPay malipopay = new MaliPoPay("your-api-key");

try {
    Map<String, Object> params = new HashMap<>();
    params.put("amount", 15000);
    params.put("phone", "255754123456");
    params.put("provider", "Vodacom");

    Map<String, Object> result = malipopay.payments().collect(params);

    System.out.println("Reference: " + result.get("reference"));
    System.out.println("Status: " + result.get("status"));
} catch (MaliPoPayException e) {
    System.err.println("Payment failed: " + e.getMessage());
}
```

### Collection with Metadata

Attach additional data for your own record-keeping:

```java
Map<String, Object> params = new HashMap<>();
params.put("amount", 50000);
params.put("phone", "255678901234");
params.put("provider", "Airtel");
params.put("description", "Monthly subscription - Premium Plan");
params.put("reference", "SUB-2024-00123");

Map<String, Object> metadata = new HashMap<>();
metadata.put("customer_id", "cust_abc123");
metadata.put("plan", "premium");
params.put("metadata", metadata);

Map<String, Object> result = malipopay.payments().collect(params);
```

### Collection for Each Provider

**M-Pesa (Vodacom)**

```java
Map<String, Object> params = new HashMap<>();
params.put("amount", 25000);
params.put("phone", "255712345678");
params.put("provider", "Vodacom");

malipopay.payments().collect(params);
```

**Airtel Money**

```java
Map<String, Object> params = new HashMap<>();
params.put("amount", 10000);
params.put("phone", "255784567890");
params.put("provider", "Airtel");

malipopay.payments().collect(params);
```

**Mixx by Yas (Tigo)**

```java
Map<String, Object> params = new HashMap<>();
params.put("amount", 7500);
params.put("phone", "255654321098");
params.put("provider", "Tigo");

malipopay.payments().collect(params);
```

**Halopesa (Halotel)**

```java
Map<String, Object> params = new HashMap<>();
params.put("amount", 3000);
params.put("phone", "255624567890");
params.put("provider", "Halotel");

malipopay.payments().collect(params);
```

**T-Pesa (TTCL)**

```java
Map<String, Object> params = new HashMap<>();
params.put("amount", 20000);
params.put("phone", "255412345678");
params.put("provider", "TTCL");

malipopay.payments().collect(params);
```

## Disbursement

Disbursement sends money from your MaliPoPay account to a mobile money wallet or bank account. Common use cases include salary payments, refunds, and agent commission payouts.

### Mobile Money Disbursement

```java
try {
    Map<String, Object> params = new HashMap<>();
    params.put("amount", 100000);
    params.put("phone", "255712345678");
    params.put("provider", "Vodacom");
    params.put("description", "Salary payment - March 2024");

    Map<String, Object> result = malipopay.payments().disburse(params);

    System.out.println("Disbursement reference: " + result.get("reference"));
} catch (MaliPoPayException e) {
    System.err.println("Disbursement failed: " + e.getMessage());
}
```

### Bank Disbursement

Send money to a bank account (CRDB, NMB, etc.):

```java
Map<String, Object> params = new HashMap<>();
params.put("amount", 500000);
params.put("accountNumber", "0150123456789");
params.put("bankCode", "CRDB");
params.put("description", "Vendor payment - Invoice #4521");

Map<String, Object> result = malipopay.payments().disburse(params);
```

## Payment Verification

After initiating a collection, always verify the final status rather than assuming success:

```java
// Store the reference when collecting
String reference = (String) result.get("reference");

// Later, verify the payment
Map<String, Object> status = malipopay.payments().verify(reference);

String paymentStatus = (String) status.get("status");

switch (paymentStatus) {
    case "SUCCESS":
        System.out.println("Payment completed! Amount: TZS " + status.get("amount"));
        // Activate the service, mark order as paid, etc.
        break;
    case "PENDING":
        System.out.println("Waiting for customer to confirm...");
        break;
    default:
        System.out.println("Payment did not go through: " + paymentStatus);
        break;
}
```

### Get Payment by Reference

Retrieve the full payment record:

```java
Map<String, Object> payment = malipopay.payments().get("PAY-2024-00123");
```

## Payment Links

Create a hosted checkout page that you share with customers via SMS, email, or WhatsApp:

```java
Map<String, Object> params = new HashMap<>();
params.put("amount", 75000);
params.put("description", "Annual gym membership");
params.put("customerName", "Juma Hamisi");
params.put("customerPhone", "255712345678");

Map<String, Object> link = malipopay.payments().createLink(params);

System.out.println("Send to customer: " + link.get("paymentUrl"));
```

### Payment Link V2

The V2 endpoint offers additional customization:

```java
Map<String, Object> params = new HashMap<>();
params.put("amount", 150000);
params.put("description", "Conference ticket - DevFest Dar 2024");
params.put("customerName", "Amina Juma");
params.put("expiresIn", 3600);  // Link expires in 1 hour

Map<String, Object> link = malipopay.payments().createLinkV2(params);
```

## Retry Failed Collections

If a collection failed due to a transient issue, retry without creating a new payment:

```java
try {
    Map<String, Object> result = malipopay.payments().retry("PAY-2024-00456");
    System.out.println("Retry initiated: " + result.get("status"));
} catch (MaliPoPayException e) {
    System.err.println("Retry failed: " + e.getMessage());
}
```

> **When to retry vs. create new:** Use `retry()` when the original payment is in a `FAILED` or `TIMEOUT` state. If the payment was `CANCELLED` by the customer, create a new collection so they get a fresh USSD prompt.

## Listing and Searching Payments

### List All Payments

```java
Map<String, Object> payments = malipopay.payments().list();

List<Map<String, Object>> items = (List<Map<String, Object>>) payments.get("items");
for (Map<String, Object> payment : items) {
    System.out.println(payment.get("reference") + " - TZS " + payment.get("amount") + " - " + payment.get("status"));
}
```

### List with Filters

```java
Map<String, Object> filters = new HashMap<>();
filters.put("page", 1);
filters.put("limit", 25);
filters.put("status", "SUCCESS");
filters.put("provider", "Vodacom");

Map<String, Object> payments = malipopay.payments().list(filters);
```

### Search Payments

```java
Map<String, Object> query = new HashMap<>();
query.put("query", "255712345678");

Map<String, Object> results = malipopay.payments().search(query);

// Search by date range
Map<String, Object> dateQuery = new HashMap<>();
dateQuery.put("startDate", "2024-01-01");
dateQuery.put("endDate", "2024-01-31");

Map<String, Object> dateResults = malipopay.payments().search(dateQuery);
```

## Typical Collection Flow

```java
import co.tz.malipopay.MaliPoPay;
import co.tz.malipopay.MaliPoPayConfig;
import co.tz.malipopay.exceptions.MaliPoPayException;
import co.tz.malipopay.exceptions.ValidationException;

import java.util.HashMap;
import java.util.Map;

MaliPoPay malipopay = new MaliPoPay("your-api-key");

// 1. Initiate the collection
String reference;
try {
    Map<String, Object> params = new HashMap<>();
    params.put("amount", 30000);
    params.put("phone", "255712345678");
    params.put("provider", "Vodacom");
    params.put("description", "Order #1234");

    Map<String, Object> result = malipopay.payments().collect(params);
    reference = (String) result.get("reference");
    // Store reference in your database

} catch (ValidationException e) {
    System.err.println("Validation error: " + e.getMessage());
    return;
} catch (MaliPoPayException e) {
    System.err.println("Failed to initiate: " + e.getMessage());
    return;
}

// 2. Set up a webhook (see webhooks.md) OR poll for the status:

// 3. Verify the payment
Map<String, Object> status = malipopay.payments().verify(reference);

if ("SUCCESS".equals(status.get("status"))) {
    // Mark order as paid, send receipt, etc.
}
```

> **Best practice:** Don't poll in a loop. Use [webhooks](webhooks.md) to receive real-time notifications when a payment completes, fails, or times out.
