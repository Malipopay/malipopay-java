# Customers

The Customers resource lets you create, retrieve, search, and verify customer records in Malipopay. Storing customers centrally means you can associate payments and invoices with known identities, avoid asking for phone numbers repeatedly, and build a clearer picture of your transaction history.

All customer methods are accessed via `malipopay.customers()`.

## Creating a Customer

```java
import co.tz.malipopay.Malipopay;
import co.tz.malipopay.exceptions.ValidationException;
import co.tz.malipopay.exceptions.MalipopayException;

import java.util.HashMap;
import java.util.Map;

Malipopay malipopay = new Malipopay("your-api-key");

try {
    Map<String, Object> params = new HashMap<>();
    params.put("name", "Juma Hamisi");
    params.put("phoneNumber", "255712345678");
    params.put("email", "juma.hamisi@example.com");
    params.put("address", "Mikocheni, Dar es Salaam");

    Map<String, Object> customer = malipopay.customers().create(params);

    System.out.println("Customer created: " + customer.get("customerNo"));
} catch (ValidationException e) {
    System.err.println("Validation error: " + e.getMessage());
    if (e.getFields() != null) {
        e.getFields().forEach((field, msg) ->
            System.err.println("  " + field + ": " + msg)
        );
    }
} catch (MalipopayException e) {
    System.err.println("Error: " + e.getMessage());
}
```

### Why Create Customers?

You can collect payments without creating a customer record first -- just pass the phone number to `payments().collect()`. However, creating customers is useful when:

- You need to track payment history per person.
- You send recurring invoices and want to pre-fill recipient details.
- You want to verify a customer's mobile money identity before charging them.
- Your business has a CRM and you want a single customer ID that ties your system to Malipopay.

## Retrieving a Customer

### By ID

```java
Map<String, Object> customer = malipopay.customers().get("64a1b2c3d4e5f6a7b8c9d0e1");
System.out.println(customer.get("name") + " - " + customer.get("phoneNumber"));
```

### By Customer Number

```java
Map<String, Object> customer = malipopay.customers().getByNumber("CUST-0001");
```

### By Phone Number

```java
Map<String, Object> customer = malipopay.customers().getByPhone("255712345678");
```

## Listing Customers

```java
// List all customers (paginated)
Map<String, Object> customers = malipopay.customers().list();

List<Map<String, Object>> items = (List<Map<String, Object>>) customers.get("items");
for (Map<String, Object> customer : items) {
    System.out.println(customer.get("customerNo") + " - " + customer.get("name"));
}

// With pagination
Map<String, Object> filters = new HashMap<>();
filters.put("page", 2);
filters.put("limit", 50);

Map<String, Object> page2 = malipopay.customers().list(filters);
```

## Searching Customers

Search across customer name, phone number, email, or customer number:

```java
Map<String, Object> query = new HashMap<>();
query.put("query", "Juma");

List<Map<String, Object>> results = malipopay.customers().search(query);

for (Map<String, Object> customer : results) {
    System.out.println(customer.get("name") + " (" + customer.get("phoneNumber") + ")");
}
```

## Verifying a Customer

Verification checks whether a phone number is registered on a mobile money network and returns the registered name. This helps you confirm legitimacy before charging:

```java
try {
    Map<String, Object> verification = malipopay.customers().verify("255712345678");

    System.out.println("Registered name: " + verification.get("name"));
    System.out.println("Network: " + verification.get("provider"));
} catch (MalipopayException e) {
    System.err.println("Verification failed: " + e.getMessage());
}
```

### When to Verify

- **Before first payment:** Make sure the customer is reachable on mobile money.
- **During onboarding:** Show the registered name and ask for confirmation -- builds trust.
- **Fraud prevention:** If the registered name doesn't match, investigate before processing.

## Complete Example: Onboarding Flow

```java
import co.tz.malipopay.Malipopay;
import co.tz.malipopay.exceptions.MalipopayException;
import co.tz.malipopay.exceptions.NotFoundException;

import java.util.HashMap;
import java.util.Map;

Malipopay malipopay = new Malipopay("your-api-key");
String phone = "255712345678";

String customerId;

// 1. Check if the customer already exists
try {
    Map<String, Object> existing = malipopay.customers().getByPhone(phone);
    System.out.println("Welcome back, " + existing.get("name") + "!");
    customerId = (String) existing.get("_id");

} catch (NotFoundException e) {
    // 2. New customer -- verify their phone first
    try {
        Map<String, Object> verification = malipopay.customers().verify(phone);
        System.out.println("We found you as: " + verification.get("name"));
    } catch (MalipopayException ex) {
        System.out.println("Could not verify phone number.");
    }

    // 3. Create the customer record
    Map<String, Object> params = new HashMap<>();
    params.put("name", "Juma Hamisi");
    params.put("phoneNumber", phone);
    params.put("email", "juma@example.com");

    Map<String, Object> customer = malipopay.customers().create(params);
    customerId = (String) customer.get("_id");
    System.out.println("Customer created: " + customer.get("customerNo"));
}

// 4. Collect a payment
Map<String, Object> paymentParams = new HashMap<>();
paymentParams.put("amount", 10000);
paymentParams.put("phone", phone);
paymentParams.put("provider", "Vodacom");

Map<String, Object> payment = malipopay.payments().collect(paymentParams);
System.out.println("Payment initiated: " + payment.get("reference"));
```
