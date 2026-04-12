# Error Handling

The MaliPoPay Java SDK uses exceptions to signal errors. Every exception extends the base `MaliPoPayException` class, so you can catch all SDK errors in one place or handle specific types individually.

## Exception Hierarchy

```
MaliPoPayException (extends RuntimeException)
  |-- AuthenticationException    (HTTP 401)
  |-- PermissionException        (HTTP 403)
  |-- NotFoundException          (HTTP 404)
  |-- ValidationException        (HTTP 422)
  |-- RateLimitException         (HTTP 429)
  |-- ApiException               (HTTP 5xx)
  |-- ConnectionException        (network/timeout)
```

All exception classes live under `co.tz.malipopay.exceptions`.

## Catching All Errors

The simplest approach -- catch `MaliPoPayException` to handle any SDK error:

```java
import co.tz.malipopay.MaliPoPay;
import co.tz.malipopay.exceptions.MaliPoPayException;

MaliPoPay malipopay = new MaliPoPay("your-api-key");

try {
    Map<String, Object> params = new HashMap<>();
    params.put("amount", 15000);
    params.put("phone", "255712345678");
    params.put("provider", "Vodacom");

    Map<String, Object> result = malipopay.payments().collect(params);
} catch (MaliPoPayException e) {
    System.err.println("Error: " + e.getMessage());
    System.err.println("HTTP status: " + e.getStatusCode());
}
```

## Handling Specific Exceptions

### AuthenticationException (401)

Thrown when your API key is invalid, expired, or missing.

```java
import co.tz.malipopay.exceptions.AuthenticationException;

try {
    malipopay.payments().collect(params);
} catch (AuthenticationException e) {
    // API key is wrong -- don't retry, fix the key
    System.err.println("Auth failed: " + e.getMessage());
    // Alert your team -- key may have been rotated
}
```

**Common causes:**
- Using a UAT key against production (or vice versa).
- The API key was regenerated in the dashboard.
- The `apiToken` header is missing or malformed.

### PermissionException (403)

Thrown when your API key is valid but lacks permission for the action.

```java
import co.tz.malipopay.exceptions.PermissionException;

try {
    malipopay.payments().disburse(params);
} catch (PermissionException e) {
    System.err.println("Permission denied: " + e.getMessage());
    // Contact MaliPoPay to enable disbursement
}
```

### NotFoundException (404)

Thrown when the resource doesn't exist.

```java
import co.tz.malipopay.exceptions.NotFoundException;

try {
    malipopay.payments().get("PAY-DOES-NOT-EXIST");
} catch (NotFoundException e) {
    System.err.println("Not found: " + e.getMessage());
}
```

### ValidationException (422)

Thrown when request parameters are invalid. Provides access to field-level errors:

```java
import co.tz.malipopay.exceptions.ValidationException;

try {
    Map<String, Object> params = new HashMap<>();
    params.put("amount", -500);
    params.put("phone", "07123");
    params.put("provider", "InvalidNetwork");

    malipopay.payments().collect(params);
} catch (ValidationException e) {
    System.err.println("Validation failed: " + e.getMessage());

    Map<String, String> fields = e.getFields();
    if (fields != null) {
        fields.forEach((field, message) ->
            System.err.println("  " + field + ": " + message)
        );
    }
}
```

### RateLimitException (429)

Thrown when you've sent too many requests.

```java
import co.tz.malipopay.exceptions.RateLimitException;

try {
    malipopay.payments().collect(params);
} catch (RateLimitException e) {
    int retryAfter = e.getRetryAfter(); // seconds
    System.err.println("Rate limited. Retry after " + retryAfter + " seconds.");

    Thread.sleep(retryAfter * 1000L);
    // Then retry
}
```

### ApiException (5xx)

Thrown when the MaliPoPay server encounters an internal error. These are transient -- retrying usually works.

```java
import co.tz.malipopay.exceptions.ApiException;

try {
    malipopay.payments().collect(params);
} catch (ApiException e) {
    System.err.println("Server error: " + e.getMessage());
    // Safe to retry with backoff
}
```

### ConnectionException

Thrown when the SDK can't connect to the API -- DNS failure, timeout, SSL error, etc.

```java
import co.tz.malipopay.exceptions.ConnectionException;

try {
    malipopay.payments().collect(params);
} catch (ConnectionException e) {
    System.err.println("Network error: " + e.getMessage());
    // Check server connectivity, increase timeout if needed
}
```

## Comprehensive Try/Catch Pattern

For production code, handle from most specific to least specific:

```java
import co.tz.malipopay.exceptions.*;

try {
    Map<String, Object> params = new HashMap<>();
    params.put("amount", 30000);
    params.put("phone", "255712345678");
    params.put("provider", "Vodacom");

    Map<String, Object> result = malipopay.payments().collect(params);
    System.out.println("Payment initiated: " + result.get("reference"));

} catch (ValidationException e) {
    System.err.println("Invalid input: " + e.getMessage());
    if (e.getFields() != null) {
        e.getFields().forEach((f, m) -> System.err.println("  - " + f + ": " + m));
    }

} catch (AuthenticationException e) {
    System.err.println("Authentication failed. Check your API key.");

} catch (PermissionException e) {
    System.err.println("Permission denied: " + e.getMessage());

} catch (NotFoundException e) {
    System.err.println("Not found: " + e.getMessage());

} catch (RateLimitException e) {
    System.err.println("Rate limited. Retry in " + e.getRetryAfter() + "s");

} catch (ApiException e) {
    System.err.println("Server error: " + e.getMessage());

} catch (ConnectionException e) {
    System.err.println("Network error: " + e.getMessage());

} catch (MaliPoPayException e) {
    System.err.println("Unexpected error: " + e.getMessage());
}
```

## Retry Strategies

| Exception | Retry? | Strategy |
|-----------|--------|----------|
| `AuthenticationException` | No | Fix your API key. |
| `PermissionException` | No | Contact MaliPoPay support. |
| `NotFoundException` | No | Check the ID/reference. |
| `ValidationException` | No | Fix the request parameters. |
| `RateLimitException` | Yes | Wait `getRetryAfter()` seconds. |
| `ApiException` | Yes | Exponential backoff (1s, 2s, 4s, 8s). |
| `ConnectionException` | Yes | Backoff. Check network if persistent. |

### Exponential Backoff Example

```java
import co.tz.malipopay.MaliPoPay;
import co.tz.malipopay.exceptions.*;

import java.util.Map;

public class RetryHelper {

    public static Map<String, Object> collectWithRetry(
            MaliPoPay malipopay,
            Map<String, Object> params,
            int maxRetries) throws MaliPoPayException {

        int attempt = 0;

        while (true) {
            try {
                return malipopay.payments().collect(params);

            } catch (RateLimitException e) {
                attempt++;
                if (attempt > maxRetries) throw e;

                try {
                    Thread.sleep(e.getRetryAfter() * 1000L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }

            } catch (ApiException | ConnectionException e) {
                attempt++;
                if (attempt > maxRetries) throw e;

                long delay = Math.min((long) Math.pow(2, attempt) * 1000, 30000);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw (MaliPoPayException) e;
                }
            }
            // Validation, Auth, Permission, NotFound -- propagate immediately
        }
    }
}
```

Usage:

```java
Map<String, Object> params = new HashMap<>();
params.put("amount", 25000);
params.put("phone", "255712345678");
params.put("provider", "Vodacom");

Map<String, Object> result = RetryHelper.collectWithRetry(malipopay, params, 3);
```

## Common Errors and Solutions

| Error Message | Cause | Solution |
|---------------|-------|----------|
| `Invalid API key` | Wrong or expired key | Regenerate in the dashboard |
| `Phone number must start with 255` | Wrong format | Use `255712345678`, not `0712345678` |
| `Amount must be at least 1000` | Amount too low | Check provider minimum |
| `Invalid provider` | Typo in provider | Use: `Vodacom`, `Airtel`, `Tigo`, `Halotel`, `TTCL` |
| `Insufficient balance` | Low account balance | Top up in the dashboard |
| `Duplicate reference` | Reference already used | Use a unique reference per payment |
| `Request timeout` | API didn't respond in time | Increase timeout or retry |
