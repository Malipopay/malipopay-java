# MaliPoPay Java SDK

Official Java SDK for the [MaliPoPay](https://malipopay.co.tz) payment platform (Tanzania).

Requires **Java 11+**.

## Installation

### Maven

```xml
<dependency>
    <groupId>co.tz.malipopay</groupId>
    <artifactId>malipopay-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'co.tz.malipopay:malipopay-java:1.0.0'
```

## Quick Start

```java
import co.tz.malipopay.MaliPoPay;
import co.tz.malipopay.ApiResponse;

import java.util.Map;

MaliPoPay client = new MaliPoPay("your-api-key");

// Collect mobile money
ApiResponse<Object> response = client.payments().collect(Map.of(
    "amount", 5000,
    "phone", "255712345678",
    "provider", "Vodacom",
    "reference", "ORDER-001"
));

System.out.println(response.getData());
```

## Configuration

```java
import co.tz.malipopay.MaliPoPayConfig;

MaliPoPayConfig config = new MaliPoPayConfig.Builder()
    .environment(MaliPoPayConfig.Environment.UAT)  // or PRODUCTION (default)
    .timeout(60000)          // request timeout in ms (default: 30000)
    .retries(5)              // retry count for 5xx/network errors (default: 3)
    .webhookSecret("whsec_xxx")
    .build();

MaliPoPay client = new MaliPoPay("your-api-key", config);
```

You can also override the base URL directly:

```java
MaliPoPayConfig config = new MaliPoPayConfig.Builder()
    .baseUrl("https://custom-api.example.com")
    .build();
```

## Resources

### Payments

```java
// Initiate a payment
client.payments().initiate(params);

// Collect mobile money
client.payments().collect(params);

// Disburse funds
client.payments().disburse(params);

// Pay now
client.payments().payNow(params);

// Verify a payment
client.payments().verify("reference");

// Get payment by reference
client.payments().get("reference");

// List payments
client.payments().list();
client.payments().list(queryParams);

// Search payments
client.payments().search(queryParams);

// Approve a payment
client.payments().approve(params);

// Retry a failed payment
client.payments().retry("reference");

// Create a payment link
client.payments().createLink(params);
```

### Customers

```java
client.customers().create(params);
client.customers().list();
client.customers().get("customer-id");
client.customers().search(queryParams);
client.customers().verify(params);
```

### Invoices

```java
client.invoices().create(params);
client.invoices().list();
client.invoices().get("invoice-id");
client.invoices().recordPayment(params);
client.invoices().approveDraft(params);
```

### Products

```java
client.products().create(params);
client.products().list();
client.products().get("product-id");
```

### Transactions

```java
client.transactions().list();
client.transactions().get("transaction-id");
client.transactions().search(queryParams);
```

### Account

```java
client.account().transactions();
client.account().reconciliation();
```

### SMS

```java
client.sms().send(params);
client.sms().sendBulk(params);
client.sms().schedule(params);
```

### References

```java
client.references().banks();
client.references().currencies();
client.references().countries();
client.references().institutions();
```

## Webhooks

Verify and parse incoming webhook events:

```java
import co.tz.malipopay.webhooks.Webhooks;

// Via the client (uses config webhookSecret)
boolean valid = client.webhooks().verify(rawBody, signatureHeader);
Map<String, Object> event = client.webhooks().constructEvent(rawBody, signatureHeader);

// Or standalone
Webhooks webhooks = new Webhooks("whsec_your_secret");
Map<String, Object> event = webhooks.constructEvent(rawBody, signatureHeader);
```

## Error Handling

The SDK throws typed exceptions for different error scenarios:

```java
import co.tz.malipopay.exceptions.*;

try {
    client.payments().collect(params);
} catch (AuthenticationException e) {
    // 401 - Invalid API key
} catch (ValidationException e) {
    // 422 - Invalid parameters
} catch (NotFoundException e) {
    // 404 - Resource not found
} catch (RateLimitException e) {
    // 429 - Too many requests
} catch (ApiException e) {
    // 5xx - Server error
} catch (ConnectionException e) {
    // Network error
} catch (MaliPoPayException e) {
    // Any other MaliPoPay error
    System.err.println("Status: " + e.getStatusCode());
    System.err.println("Code: " + e.getCode());
    System.err.println("Details: " + e.getDetails());
}
```

## Environments

| Environment | Base URL |
|---|---|
| Production | `https://core-prod.malipopay.co.tz` |
| UAT | `https://core-uat.malipopay.co.tz` |

## License

MIT - Lockwood Technology Ltd
