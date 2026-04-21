# Getting Started with Malipopay Java SDK

## Prerequisites

- **Java 11 or higher** -- The SDK uses modern Java features like `var`, `HttpClient`, and records.
- **Maven or Gradle** -- For dependency management.
- A Malipopay account with an API key.

## Installation

### Maven

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>tz.co.malipopay</groupId>
    <artifactId>malipopay-java</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Gradle

Add the dependency to your `build.gradle`:

```groovy
implementation 'tz.co.malipopay:malipopay-java:1.1.0'
```

Or with Kotlin DSL (`build.gradle.kts`):

```kotlin
implementation("tz.co.malipopay:malipopay-java:1.1.0")
```

## Getting Your API Key

1. Sign in to the Malipopay dashboard at [app.malipopay.co.tz](https://app.malipopay.co.tz).
2. Navigate to **Settings > API Keys**.
3. Copy your API key. Keep it secret -- treat it like a password.

> **Tip:** Malipopay provides two environments. Your dashboard will show separate API keys for **Production** and **UAT (testing)**. Always start with UAT during development so you don't trigger real mobile money transactions.

## Your First Payment

Here is the fastest way to collect a mobile money payment:

```java
import tz.co.malipopay.Malipopay;
import tz.co.malipopay.MalipopayConfig;

import java.util.HashMap;
import java.util.Map;

public class QuickStart {
    public static void main(String[] args) {
        Malipopay malipopay = new Malipopay("your-api-key");

        Map<String, Object> params = new HashMap<>();
        params.put("amount", 5000);
        params.put("phone", "255712345678");
        params.put("provider", "Vodacom");  // M-Pesa

        Map<String, Object> result = malipopay.payments().collect(params);

        System.out.println("Payment reference: " + result.get("reference"));
    }
}
```

When this runs, the customer at `255712345678` receives a USSD push prompt on their phone asking them to confirm the TZS 5,000 payment via M-Pesa.

## Using the Config Builder

For more control, use `MalipopayConfig.builder()`:

```java
import tz.co.malipopay.Malipopay;
import tz.co.malipopay.MalipopayConfig;

MalipopayConfig config = MalipopayConfig.builder()
        .apiKey("your-api-key")
        .environment("uat")       // Use UAT for testing
        .timeout(30)              // 30-second timeout
        .retries(2)               // Retry transient failures twice
        .build();

Malipopay malipopay = new Malipopay(config);
```

## Choosing an Environment

By default the SDK connects to **production**. During development, use the **UAT** environment:

```java
// UAT / testing
MalipopayConfig config = MalipopayConfig.builder()
        .apiKey("your-uat-api-key")
        .environment("uat")
        .build();

// Production (default)
MalipopayConfig config = MalipopayConfig.builder()
        .apiKey("your-production-api-key")
        .environment("production")
        .build();
```

The two environments are completely separate:

| Environment | Base URL |
|-------------|----------|
| Production  | `https://core-prod.malipopay.co.tz` |
| UAT         | `https://core-uat.malipopay.co.tz`  |

If you need a custom URL (for example, a local proxy), use the `baseUrl` option:

```java
MalipopayConfig config = MalipopayConfig.builder()
        .apiKey("your-api-key")
        .baseUrl("http://localhost:8080")
        .build();
```

## Spring Boot Integration

If you're using Spring Boot, create a configuration bean:

```java
import tz.co.malipopay.Malipopay;
import tz.co.malipopay.MalipopayConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MalipopayConfiguration {

    @Value("${malipopay.api-key}")
    private String apiKey;

    @Value("${malipopay.environment:production}")
    private String environment;

    @Bean
    public Malipopay malipopay() {
        MalipopayConfig config = MalipopayConfig.builder()
                .apiKey(apiKey)
                .environment(environment)
                .build();

        return new Malipopay(config);
    }
}
```

Then in `application.yml`:

```yaml
malipopay:
  api-key: ${MALIPOPAY_API_KEY}
  environment: uat
```

Now inject `Malipopay` anywhere in your application:

```java
@Service
public class PaymentService {
    private final Malipopay malipopay;

    public PaymentService(Malipopay malipopay) {
        this.malipopay = malipopay;
    }

    public String collectPayment(int amount, String phone, String provider) {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount);
        params.put("phone", phone);
        params.put("provider", provider);

        Map<String, Object> result = malipopay.payments().collect(params);
        return (String) result.get("reference");
    }
}
```

## What's Next?

- [Payments](payments.md) -- Collect mobile money, disburse funds, verify transactions.
- [Customers](customers.md) -- Create and manage customer records.
- [Invoices](invoices.md) -- Generate invoices with line items and tax.
- [SMS](sms.md) -- Send transactional and bulk SMS messages.
- [Webhooks](webhooks.md) -- Receive real-time payment notifications.
- [Error Handling](error-handling.md) -- Handle failures gracefully.
- [Configuration](configuration.md) -- Fine-tune timeouts, retries, and more.
