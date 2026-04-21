package tz.co.malipopay;

import tz.co.malipopay.exceptions.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * Internal HTTP client that handles all communication with the Malipopay API.
 * Uses Java 11+ {@link java.net.http.HttpClient}. Not intended for public use.
 */
public class HttpClient {

    private final String apiKey;
    private final String baseUrl;
    private final int timeout;
    private final int retries;
    private final java.net.http.HttpClient client;
    private final Gson gson;

    public HttpClient(String apiKey, MalipopayConfig config) {
        this.apiKey = apiKey;
        this.baseUrl = config.getBaseUrl();
        this.timeout = config.getTimeout();
        this.retries = config.getRetries();
        this.gson = new Gson();
        this.client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeout))
                .build();
    }

    // ---- Public API ----

    public ApiResponse<Object> get(String path) {
        return get(path, null);
    }

    public ApiResponse<Object> get(String path, Map<String, Object> queryParams) {
        String url = buildUrl(path, queryParams);
        HttpRequest request = newRequestBuilder(url)
                .GET()
                .build();
        return executeWithRetry(request);
    }

    public ApiResponse<Object> post(String path, Map<String, Object> body) {
        String url = buildUrl(path, null);
        String json = body != null ? gson.toJson(body) : "{}";
        HttpRequest request = newRequestBuilder(url)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        return executeWithRetry(request);
    }

    // ---- Internals ----

    private HttpRequest.Builder newRequestBuilder(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(timeout))
                .header("apiToken", apiKey)
                .header("Accept", "application/json");
    }

    private String buildUrl(String path, Map<String, Object> queryParams) {
        StringBuilder sb = new StringBuilder(baseUrl);
        if (!path.startsWith("/")) {
            sb.append("/");
        }
        sb.append(path);

        if (queryParams != null && !queryParams.isEmpty()) {
            sb.append("?");
            boolean first = true;
            for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
                if (!first) sb.append("&");
                sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
                sb.append("=");
                sb.append(URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8));
                first = false;
            }
        }
        return sb.toString();
    }

    private ApiResponse<Object> executeWithRetry(HttpRequest request) {
        MalipopayException lastException = null;

        for (int attempt = 0; attempt <= retries; attempt++) {
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return handleResponse(response);
            } catch (MalipopayException e) {
                // Do not retry client errors (4xx)
                if (e.getStatusCode() >= 400 && e.getStatusCode() < 500) {
                    throw e;
                }
                lastException = e;
            } catch (IOException e) {
                lastException = new ConnectionException("Network error: " + e.getMessage(), e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ConnectionException("Request interrupted", e);
            }

            // Exponential back-off before retry
            if (attempt < retries) {
                try {
                    Thread.sleep((long) Math.pow(2, attempt) * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ConnectionException("Retry interrupted", ie);
                }
            }
        }

        throw lastException != null ? lastException : new ConnectionException("Request failed after retries");
    }

    private ApiResponse<Object> handleResponse(HttpResponse<String> response) {
        int status = response.statusCode();
        String body = response.body();

        if (status >= 200 && status < 300) {
            return parseSuccessResponse(status, body);
        }

        // Parse error body for message
        String errorMessage = "API error";
        String errorDetails = body;
        try {
            JsonObject json = gson.fromJson(body, JsonObject.class);
            if (json != null && json.has("message")) {
                errorMessage = json.get("message").getAsString();
            }
        } catch (Exception ignored) {
            // use defaults
        }

        switch (status) {
            case 401:
                throw new AuthenticationException(errorMessage, errorDetails);
            case 404:
                throw new NotFoundException(errorMessage, errorDetails);
            case 422:
                throw new ValidationException(errorMessage, errorDetails);
            case 429:
                throw new RateLimitException(errorMessage, errorDetails);
            default:
                if (status >= 500) {
                    throw new ApiException(errorMessage, status, errorDetails);
                }
                throw new MalipopayException(errorMessage, status, "UNKNOWN_ERROR", errorDetails);
        }
    }

    @SuppressWarnings("unchecked")
    private ApiResponse<Object> parseSuccessResponse(int status, String body) {
        try {
            JsonObject json = gson.fromJson(body, JsonObject.class);
            if (json == null) {
                return new ApiResponse<>(true, status, "OK", null);
            }

            boolean success = json.has("success") ? json.get("success").getAsBoolean() : true;
            String message = json.has("message") ? json.get("message").getAsString() : "OK";
            Object data = null;

            if (json.has("data")) {
                JsonElement dataEl = json.get("data");
                data = gson.fromJson(dataEl, Object.class);
            } else {
                // Return the whole object as data if there is no data field
                data = gson.fromJson(json, Object.class);
            }

            return new ApiResponse<>(success, status, message, data);
        } catch (Exception e) {
            // If body is not JSON, return raw string
            return new ApiResponse<>(true, status, "OK", body);
        }
    }

    /**
     * Exposes the Gson instance for resource classes that may need serialization.
     */
    public Gson getGson() {
        return gson;
    }
}
