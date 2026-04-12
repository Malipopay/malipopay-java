# Webhooks

Webhooks let MaliPoPay push real-time notifications to your server when events happen -- a payment completes, a disbursement fails, an invoice is paid. Instead of polling the API, you set up an endpoint and MaliPoPay sends an HTTP POST request with the event data.

## Setting Up a Webhook Endpoint

1. Sign in to the MaliPoPay dashboard at [app.malipopay.co.tz](https://app.malipopay.co.tz).
2. Navigate to **Settings > Webhooks**.
3. Enter your endpoint URL (e.g., `https://yourapp.co.tz/webhooks/malipopay`).
4. Select the events you want to receive.
5. Copy the **webhook secret** -- you'll use this to verify incoming requests.

> **Important:** Your endpoint must be publicly accessible over HTTPS. MaliPoPay will not deliver webhooks to `http://` URLs in production.

## Spring Boot Example

### Controller

```java
import co.tz.malipopay.Webhooks;
import co.tz.malipopay.exceptions.WebhookVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhooks")
public class MaliPoPayWebhookController {

    @Value("${malipopay.webhook-secret}")
    private String webhookSecret;

    @PostMapping("/malipopay")
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-MaliPoPay-Signature", defaultValue = "") String signature) {

        // 1. Verify the signature
        Webhooks webhooks = new Webhooks(webhookSecret);

        Map<String, Object> event;
        try {
            event = webhooks.verify(payload, signature);
        } catch (WebhookVerificationException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid signature"));
        }

        // 2. Respond immediately, then process async
        String eventType = (String) event.get("event");
        Map<String, Object> data = (Map<String, Object>) event.get("data");

        switch (eventType) {
            case "payment.completed" -> handlePaymentCompleted(data);
            case "payment.failed" -> handlePaymentFailed(data);
            case "disbursement.completed" -> handleDisbursementCompleted(data);
            case "disbursement.failed" -> handleDisbursementFailed(data);
            case "invoice.paid" -> handleInvoicePaid(data);
            default -> System.out.println("Unhandled event: " + eventType);
        }

        return ResponseEntity.ok(Map.of("received", true));
    }

    private void handlePaymentCompleted(Map<String, Object> data) {
        String reference = (String) data.get("reference");
        Object amount = data.get("amount");
        System.out.println("Payment completed: " + reference + " - TZS " + amount);
        // Mark order as paid, send receipt, etc.
    }

    private void handlePaymentFailed(Map<String, Object> data) {
        String reference = (String) data.get("reference");
        String reason = (String) data.getOrDefault("reason", "Unknown");
        System.out.println("Payment failed: " + reference + " - " + reason);
        // Notify the customer, retry, or cancel the order
    }

    private void handleDisbursementCompleted(Map<String, Object> data) {
        System.out.println("Disbursement completed: " + data.get("reference"));
    }

    private void handleDisbursementFailed(Map<String, Object> data) {
        System.out.println("Disbursement failed: " + data.get("reference"));
    }

    private void handleInvoicePaid(Map<String, Object> data) {
        System.out.println("Invoice paid: " + data.get("invoiceNo"));
    }
}
```

### Application Configuration

In `application.yml`:

```yaml
malipopay:
  api-key: ${MALIPOPAY_API_KEY}
  webhook-secret: ${MALIPOPAY_WEBHOOK_SECRET}
```

### Async Processing with Spring Events

For better performance, dispatch to a Spring event listener so the controller returns immediately:

```java
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhooks")
public class MaliPoPayWebhookController {

    private final ApplicationEventPublisher eventPublisher;
    private final Webhooks webhooks;

    public MaliPoPayWebhookController(
            ApplicationEventPublisher eventPublisher,
            @Value("${malipopay.webhook-secret}") String webhookSecret) {
        this.eventPublisher = eventPublisher;
        this.webhooks = new Webhooks(webhookSecret);
    }

    @PostMapping("/malipopay")
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-MaliPoPay-Signature", defaultValue = "") String signature) {

        Map<String, Object> event;
        try {
            event = webhooks.verify(payload, signature);
        } catch (WebhookVerificationException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid signature"));
        }

        // Publish the event -- listeners process it asynchronously
        eventPublisher.publishEvent(new MaliPoPayWebhookEvent(event));

        return ResponseEntity.ok(Map.of("received", true));
    }
}
```

```java
// Event class
public record MaliPoPayWebhookEvent(Map<String, Object> payload) {}
```

```java
// Async listener
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class MaliPoPayEventListener {

    @Async
    @EventListener
    public void onWebhookReceived(MaliPoPayWebhookEvent event) {
        Map<String, Object> payload = event.payload();
        String eventType = (String) payload.get("event");
        Map<String, Object> data = (Map<String, Object>) payload.get("data");

        switch (eventType) {
            case "payment.completed" -> processPaymentCompleted(data);
            case "payment.failed" -> processPaymentFailed(data);
            // ... handle other events
        }
    }

    private void processPaymentCompleted(Map<String, Object> data) {
        // Update the order, send a receipt, etc.
    }

    private void processPaymentFailed(Map<String, Object> data) {
        // Notify the customer, retry, etc.
    }
}
```

## Raw Servlet Example

If you're not using Spring Boot:

```java
import co.tz.malipopay.Webhooks;
import co.tz.malipopay.exceptions.WebhookVerificationException;

import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class MaliPoPayWebhookServlet extends HttpServlet {

    private static final String WEBHOOK_SECRET = System.getenv("MALIPOPAY_WEBHOOK_SECRET");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 1. Read the raw body
        String payload;
        try (BufferedReader reader = req.getReader()) {
            payload = reader.lines().collect(Collectors.joining("\n"));
        }

        String signature = req.getHeader("X-MaliPoPay-Signature");
        if (signature == null) {
            signature = "";
        }

        // 2. Verify the signature
        Webhooks webhooks = new Webhooks(WEBHOOK_SECRET);

        Map<String, Object> event;
        try {
            event = webhooks.verify(payload, signature);
        } catch (WebhookVerificationException e) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\": \"Invalid signature\"}");
            return;
        }

        // 3. Respond 200 immediately
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"received\": true}");

        // 4. Process the event
        String eventType = (String) event.get("event");
        Map<String, Object> data = (Map<String, Object>) event.get("data");

        switch (eventType) {
            case "payment.completed":
                System.out.println("Payment completed: " + data.get("reference"));
                break;
            case "payment.failed":
                System.out.println("Payment failed: " + data.get("reference"));
                break;
            default:
                System.out.println("Unhandled event: " + eventType);
        }
    }
}
```

## Signature Verification

The `Webhooks` class verifies that webhook requests genuinely come from MaliPoPay using HMAC-SHA256:

```java
Webhooks webhooks = new Webhooks("whsec_your_webhook_secret");

try {
    Map<String, Object> event = webhooks.verify(payload, signature);
    // Signature is valid -- process the event
} catch (WebhookVerificationException e) {
    // Signature is invalid -- reject the request
}
```

Never skip verification, even during development.

## Event Types

| Event | Description |
|-------|-------------|
| `payment.completed` | A mobile money collection was confirmed and processed. |
| `payment.failed` | A collection failed -- customer cancelled, insufficient balance, or timeout. |
| `payment.pending` | A collection is waiting for the customer to confirm. |
| `disbursement.completed` | A payout was delivered to the recipient. |
| `disbursement.failed` | A payout could not be delivered. |
| `invoice.paid` | An invoice was fully paid. |
| `invoice.partially_paid` | A partial payment was recorded against an invoice. |
| `invoice.overdue` | An invoice has passed its due date without being fully paid. |

### Event Payload Structure

```json
{
    "event": "payment.completed",
    "timestamp": "2024-03-15T10:30:00Z",
    "data": {
        "reference": "PAY-2024-00456",
        "amount": 25000,
        "phone": "255712345678",
        "provider": "Vodacom",
        "status": "SUCCESS",
        "metadata": {
            "order_id": "ORD-1234"
        }
    }
}
```

## Best Practices

### Respond 200 First

MaliPoPay waits up to 10 seconds for a response. Return `200` immediately, then process asynchronously. In Spring Boot, use `@Async` event listeners or submit work to a thread pool. In a servlet, respond before processing.

### Implement Idempotency

MaliPoPay may deliver the same event more than once. Use the payment reference as an idempotency key:

```java
String reference = (String) data.get("reference");

// Check if already processed (using your database)
if (webhookRepository.existsByReference(reference)) {
    return; // Already handled
}

// Process and record
processPayment(data);
webhookRepository.save(new ProcessedWebhook(reference, eventType));
```

### Always Verify Signatures

Never skip signature verification, even in development.

### Log Everything

```java
logger.info("Webhook received: event={}, reference={}",
    event.get("event"),
    ((Map<String, Object>) event.get("data")).get("reference"));
```

### Use HTTPS

Production webhooks are only delivered to HTTPS endpoints. During development, use [ngrok](https://ngrok.com):

```bash
ngrok http 8080
# Use the generated https://xxxx.ngrok.io/webhooks/malipopay URL
```
