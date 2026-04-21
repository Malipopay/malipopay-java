package tz.co.malipopay.resources;

import tz.co.malipopay.ApiResponse;
import tz.co.malipopay.HttpClient;

import java.util.Map;

/**
 * Invoice resource — create, list, get, record payment, and approve invoices.
 */
public class Invoices {

    private final HttpClient client;

    public Invoices(HttpClient client) {
        this.client = client;
    }

    /**
     * Create a new invoice.
     */
    public ApiResponse<Object> create(Map<String, Object> params) {
        return client.post("/api/v1/invoice", params);
    }

    /**
     * List invoices.
     */
    public ApiResponse<Object> list() {
        return client.get("/api/v1/invoice");
    }

    /**
     * List invoices with query parameters.
     */
    public ApiResponse<Object> list(Map<String, Object> queryParams) {
        return client.get("/api/v1/invoice", queryParams);
    }

    /**
     * Get an invoice by ID.
     */
    public ApiResponse<Object> get(String id) {
        return client.get("/api/v1/invoice/" + id);
    }

    /**
     * Record a payment against an invoice.
     */
    public ApiResponse<Object> recordPayment(Map<String, Object> params) {
        return client.post("/api/v1/invoice/record-payment", params);
    }

    /**
     * Approve a draft invoice.
     */
    public ApiResponse<Object> approveDraft(Map<String, Object> params) {
        return client.post("/api/v1/invoice/approve-draft", params);
    }
}
