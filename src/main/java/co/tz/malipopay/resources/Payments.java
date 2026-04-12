package co.tz.malipopay.resources;

import co.tz.malipopay.ApiResponse;
import co.tz.malipopay.HttpClient;

import java.util.Map;

/**
 * Payment resource — initiate, collect, disburse, verify, and manage payments.
 */
public class Payments {

    private final HttpClient client;

    public Payments(HttpClient client) {
        this.client = client;
    }

    /**
     * Initiate a payment.
     */
    public ApiResponse<Object> initiate(Map<String, Object> params) {
        return client.post("/api/v1/payment", params);
    }

    /**
     * Collect a mobile money payment.
     */
    public ApiResponse<Object> collect(Map<String, Object> params) {
        return client.post("/api/v1/payment/collection", params);
    }

    /**
     * Disburse funds.
     */
    public ApiResponse<Object> disburse(Map<String, Object> params) {
        return client.post("/api/v1/payment/disbursement", params);
    }

    /**
     * Initiate a pay-now payment.
     */
    public ApiResponse<Object> payNow(Map<String, Object> params) {
        return client.post("/api/v1/payment/now", params);
    }

    /**
     * Verify a payment by reference.
     */
    public ApiResponse<Object> verify(String reference) {
        return client.get("/api/v1/payment/verify/" + reference);
    }

    /**
     * Get a payment by reference.
     */
    public ApiResponse<Object> get(String reference) {
        return client.get("/api/v1/payment/reference/" + reference);
    }

    /**
     * List payments.
     */
    public ApiResponse<Object> list() {
        return client.get("/api/v1/payment");
    }

    /**
     * List payments with query parameters.
     */
    public ApiResponse<Object> list(Map<String, Object> queryParams) {
        return client.get("/api/v1/payment", queryParams);
    }

    /**
     * Search payments.
     */
    public ApiResponse<Object> search(Map<String, Object> queryParams) {
        return client.get("/api/v1/payment/search", queryParams);
    }

    /**
     * Approve a payment.
     */
    public ApiResponse<Object> approve(Map<String, Object> params) {
        return client.post("/api/v1/payment/approve", params);
    }

    /**
     * Retry a failed payment by reference.
     */
    public ApiResponse<Object> retry(String reference) {
        return client.get("/api/v1/payment/retry/" + reference);
    }

    /**
     * Create a payment link.
     */
    public ApiResponse<Object> createLink(Map<String, Object> params) {
        return client.post("/api/v1/pay", params);
    }
}
