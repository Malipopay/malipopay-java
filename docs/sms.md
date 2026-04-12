# SMS

The SMS resource lets you send transactional and promotional messages to Tanzanian phone numbers. Use it for payment confirmations, OTPs, appointment reminders, marketing campaigns, and more.

All SMS methods are accessed via `malipopay.sms()`.

## Sending a Single SMS

```java
import co.tz.malipopay.MaliPoPay;
import co.tz.malipopay.exceptions.MaliPoPayException;

import java.util.HashMap;
import java.util.Map;

MaliPoPay malipopay = new MaliPoPay("your-api-key");

try {
    Map<String, Object> params = new HashMap<>();
    params.put("to", "255712345678");
    params.put("message", "Your payment of TZS 25,000 has been received. Reference: PAY-2024-00123. Thank you!");

    Map<String, Object> result = malipopay.sms().send(params);

    System.out.println("Message ID: " + result.get("messageId"));
    System.out.println("Status: " + result.get("status"));
} catch (MaliPoPayException e) {
    System.err.println("SMS failed: " + e.getMessage());
}
```

### With a Custom Sender ID

```java
Map<String, Object> params = new HashMap<>();
params.put("to", "255754123456");
params.put("message", "Your order #4521 has been shipped. Track it at https://yourshop.co.tz/track/4521");
params.put("senderId", "YOURSHOP");

Map<String, Object> result = malipopay.sms().send(params);
```

## Bulk SMS

Send the same message to multiple recipients at once:

```java
import java.util.List;

try {
    Map<String, Object> params = new HashMap<>();
    params.put("to", List.of(
        "255712345678",
        "255754987654",
        "255678901234",
        "255624567890"
    ));
    params.put("message", "Reminder: Your subscription renews tomorrow. Make sure your M-Pesa has sufficient balance.");
    params.put("senderId", "MYSERVICE");

    Map<String, Object> result = malipopay.sms().sendBulk(params);

    System.out.println("Batch ID: " + result.get("batchId"));
    System.out.println("Total recipients: " + result.get("totalRecipients"));
    System.out.println("Accepted: " + result.get("accepted"));
    System.out.println("Rejected: " + result.get("rejected"));
} catch (MaliPoPayException e) {
    System.err.println("Bulk SMS failed: " + e.getMessage());
}
```

### Bulk with Personalized Messages

Send different messages to each recipient:

```java
Map<String, Object> msg1 = new HashMap<>();
msg1.put("to", "255712345678");
msg1.put("message", "Hello Juma, your invoice INV-0042 for TZS 500,000 is due on April 30.");

Map<String, Object> msg2 = new HashMap<>();
msg2.put("to", "255754987654");
msg2.put("message", "Hello Amina, your invoice INV-0043 for TZS 250,000 is due on April 30.");

Map<String, Object> msg3 = new HashMap<>();
msg3.put("to", "255678901234");
msg3.put("message", "Hello Baraka, your invoice INV-0044 for TZS 175,000 is due on April 30.");

Map<String, Object> params = new HashMap<>();
params.put("messages", List.of(msg1, msg2, msg3));
params.put("senderId", "MYSERVICE");

Map<String, Object> result = malipopay.sms().sendBulk(params);
```

## Scheduled SMS

Send a message at a specific future date and time:

```java
Map<String, Object> params = new HashMap<>();
params.put("to", "255712345678");
params.put("message", "Good morning! Your appointment at Muhimbili Clinic is today at 10:00 AM. Reply YES to confirm.");
params.put("senderId", "CLINIC");
params.put("scheduledAt", "2024-04-15T07:00:00+03:00");  // EAT timezone

Map<String, Object> result = malipopay.sms().schedule(params);

System.out.println("Scheduled message ID: " + result.get("messageId"));
System.out.println("Scheduled for: " + result.get("scheduledAt"));
```

### Scheduling Bulk SMS

Combine scheduling with bulk sending for campaigns:

```java
Map<String, Object> params = new HashMap<>();
params.put("to", List.of(
    "255712345678",
    "255754987654",
    "255678901234"
));
params.put("message", "Flash sale! 30% off all items this weekend at Kariakoo Market. Shop now at https://yourshop.co.tz");
params.put("senderId", "YOURSHOP");
params.put("scheduledAt", "2024-04-19T09:00:00+03:00");

Map<String, Object> result = malipopay.sms().scheduleBulk(params);
```

## Checking SMS Status

```java
Map<String, Object> status = malipopay.sms().getStatus(messageId);

System.out.println("Status: " + status.get("status"));         // DELIVERED, SENT, FAILED, PENDING
System.out.println("Delivered at: " + status.getOrDefault("deliveredAt", "N/A"));
```

## Sender ID Guidelines

A sender ID is the name that appears as the "from" field on the recipient's phone. In Tanzania, sender IDs must follow TCRA rules:

- **Maximum 11 characters** -- alphanumeric, no spaces or special characters.
- **Must be registered** -- contact MaliPoPay support to register your sender ID.
- **Approval takes 1-3 business days** -- plan ahead.
- **Default sender ID** -- if you don't specify one, MaliPoPay uses a shared default.

Good sender IDs: `MYSHOP`, `CLINICTZ`, `BARAKA`, `PAYALERT`

Bad sender IDs: `My Shop` (spaces), `A_VERY_LONG_NAME` (too long), `$CASH$` (special characters)

## Complete Example: Payment Receipt via SMS

```java
import co.tz.malipopay.MaliPoPay;
import co.tz.malipopay.exceptions.MaliPoPayException;

import java.util.HashMap;
import java.util.Map;

MaliPoPay malipopay = new MaliPoPay("your-api-key");

// 1. Collect payment
try {
    Map<String, Object> paymentParams = new HashMap<>();
    paymentParams.put("amount", 45000);
    paymentParams.put("phone", "255712345678");
    paymentParams.put("provider", "Vodacom");

    Map<String, Object> payment = malipopay.payments().collect(paymentParams);
    String reference = (String) payment.get("reference");

    // 2. After payment confirmation (e.g., in a webhook handler), send receipt
    Map<String, Object> smsParams = new HashMap<>();
    smsParams.put("to", "255712345678");
    smsParams.put("message", "Payment confirmed! TZS 45,000 received. Ref: " + reference + ". Thank you for shopping with us.");
    smsParams.put("senderId", "MYSHOP");

    malipopay.sms().send(smsParams);

} catch (MaliPoPayException e) {
    System.err.println("Error: " + e.getMessage());
}
```

## SMS Pricing

SMS pricing depends on your MaliPoPay plan and volume. Messages to all Tanzanian networks are supported. Check your dashboard under **Settings > SMS** for current rates and balance.

> **Tip:** Use the UAT environment to test SMS integration without incurring charges. UAT messages are simulated and won't be delivered to the phone.
