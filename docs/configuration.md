# Configuration

The Malipopay Java SDK is configured through the `MalipopayConfig` builder. This page covers every available option and how to tune the SDK for your environment.

## Basic Setup

At minimum, you need your API key:

```java
import tz.co.malipopay.Malipopay;

Malipopay malipopay = new Malipopay("your-api-key");
```

This connects to the **production** environment with default settings.

## Using the Config Builder

For full control, use `MalipopayConfig.builder()`:

```java
import tz.co.malipopay.Malipopay;
import tz.co.malipopay.MalipopayConfig;

MalipopayConfig config = MalipopayConfig.builder()
        .apiKey("your-api-key")
        .environment("uat")
        .timeout(30)
        .retries(2)
        .webhookSecret("whsec_your_webhook_secret")
        .build();

Malipopay malipopay = new Malipopay(config);
```

## Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `apiKey` | `String` | *required* | Your Malipopay API key. |
| `environment` | `String` | `"production"` | Which environment to connect to: `"production"` or `"uat"`. |
| `baseUrl` | `String` | `null` | Override the base URL entirely. Takes precedence over `environment`. |
| `timeout` | `int` | `30` | HTTP request timeout in seconds. |
| `retries` | `int` | `0` | Number of automatic retries for transient errors (5xx, timeouts). |
| `webhookSecret` | `String` | `null` | Your webhook signing secret, used by the `Webhooks` class. |

## Environment Selection

Malipopay provides two separate environments:

| Environment | Base URL | Purpose |
|-------------|----------|---------|
| Production | `https://core-prod.malipopay.co.tz` | Live transactions with real money. |
| UAT | `https://core-uat.malipopay.co.tz` | Testing and development. No real money moves. |

### Using UAT for Development

Always develop and test against UAT first:

```java
// Development / testing
MalipopayConfig config = MalipopayConfig.builder()
        .apiKey("your-uat-api-key")
        .environment("uat")
        .build();

// Production
MalipopayConfig config = MalipopayConfig.builder()
        .apiKey("your-production-api-key")
        .environment("production")
        .build();
```

> **Important:** Each environment has its own API key. A UAT key won't work against production, and vice versa.

### Environment from System Properties

A common pattern is to read configuration from environment variables or system properties:

```java
MalipopayConfig config = MalipopayConfig.builder()
        .apiKey(System.getenv("MALIPOPAY_API_KEY"))
        .environment(System.getenv().getOrDefault("MALIPOPAY_ENV", "production"))
        .timeout(Integer.parseInt(System.getenv().getOrDefault("MALIPOPAY_TIMEOUT", "30")))
        .build();
```

## Spring Boot Integration

### Application Properties

```yaml
# application.yml
malipopay:
  api-key: ${MALIPOPAY_API_KEY}
  environment: ${MALIPOPAY_ENV:production}
  timeout: ${MALIPOPAY_TIMEOUT:30}
  retries: ${MALIPOPAY_RETRIES:2}
  webhook-secret: ${MALIPOPAY_WEBHOOK_SECRET:}
```

### Configuration Bean

```java
import tz.co.malipopay.Malipopay;
import tz.co.malipopay.MalipopayConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "malipopay")
public class MalipopayConfiguration {

    private String apiKey;
    private String environment = "production";
    private int timeout = 30;
    private int retries = 2;
    private String webhookSecret;

    @Bean
    public Malipopay malipopay() {
        MalipopayConfig config = MalipopayConfig.builder()
                .apiKey(apiKey)
                .environment(environment)
                .timeout(timeout)
                .retries(retries)
                .webhookSecret(webhookSecret)
                .build();

        return new Malipopay(config);
    }

    // Setters required for @ConfigurationProperties binding
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public void setTimeout(int timeout) { this.timeout = timeout; }
    public void setRetries(int retries) { this.retries = retries; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }
}
```

Now inject `Malipopay` anywhere:

```java
@Service
public class PaymentService {
    private final Malipopay malipopay;

    public PaymentService(Malipopay malipopay) {
        this.malipopay = malipopay;
    }
}
```

## Custom Base URL

Point the SDK at a custom URL -- a mock server, a local proxy, or a staging environment:

```java
MalipopayConfig config = MalipopayConfig.builder()
        .apiKey("your-api-key")
        .baseUrl("http://localhost:8080")
        .build();
```

When `baseUrl` is set, the `environment` option is ignored.

## Timeout Configuration

The default timeout is 30 seconds. Mobile money transactions involve USSD prompts, so avoid setting this too low:

```java
MalipopayConfig config = MalipopayConfig.builder()
        .apiKey("your-api-key")
        .timeout(60)  // 60 seconds
        .build();
```

If a request exceeds the timeout, the SDK throws a `ConnectionException`.

**Recommended values:**
- Normal API calls (list, search, verify): 15-30 seconds.
- Payment collection (waiting for USSD): 30-60 seconds.
- Behind a corporate proxy or firewall: 60 seconds.

## Automatic Retries

The SDK can automatically retry requests that fail due to transient errors (HTTP 5xx, timeouts):

```java
MalipopayConfig config = MalipopayConfig.builder()
        .apiKey("your-api-key")
        .retries(3)  // Initial attempt + up to 3 retries = 4 total
        .build();
```

Retries use exponential backoff (1s, 2s, 4s, etc.). Client errors (401, 403, 404, 422) are never retried.

> **Note:** If you implement your own retry logic (see [Error Handling](error-handling.md)), set `retries` to `0` to avoid double-retrying.

## Full Configuration Example

```java
import tz.co.malipopay.Malipopay;
import tz.co.malipopay.MalipopayConfig;

public class App {
    public static void main(String[] args) {
        MalipopayConfig config = MalipopayConfig.builder()
                .apiKey(System.getenv("MALIPOPAY_API_KEY"))
                .environment(System.getenv().getOrDefault("MALIPOPAY_ENV", "production"))
                .timeout(Integer.parseInt(System.getenv().getOrDefault("MALIPOPAY_TIMEOUT", "30")))
                .retries(Integer.parseInt(System.getenv().getOrDefault("MALIPOPAY_RETRIES", "2")))
                .webhookSecret(System.getenv("MALIPOPAY_WEBHOOK_SECRET"))
                .build();

        Malipopay malipopay = new Malipopay(config);

        // Ready to use
        System.out.println("Malipopay SDK initialized for " + config.getEnvironment());
    }
}
```

Set the environment variables:

```bash
export MALIPOPAY_API_KEY=mpk_live_abc123def456
export MALIPOPAY_ENV=production
export MALIPOPAY_TIMEOUT=30
export MALIPOPAY_RETRIES=2
export MALIPOPAY_WEBHOOK_SECRET=whsec_abc123def456
```

## What's Next?

- [Getting Started](getting-started.md) -- Install the SDK and make your first payment.
- [Payments](payments.md) -- Collect and disburse funds.
- [Webhooks](webhooks.md) -- Receive real-time payment notifications.
- [Error Handling](error-handling.md) -- Understand and handle API errors.
