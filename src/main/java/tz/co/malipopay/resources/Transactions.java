package tz.co.malipopay.resources;

import tz.co.malipopay.ApiResponse;
import tz.co.malipopay.HttpClient;

import java.util.Map;

/**
 * Transaction resource — list, get, and search transactions.
 */
public class Transactions {

    private final HttpClient client;

    public Transactions(HttpClient client) {
        this.client = client;
    }

    /**
     * List transactions.
     */
    public ApiResponse<Object> list() {
        return client.get("/api/v1/transactions");
    }

    /**
     * List transactions with query parameters.
     */
    public ApiResponse<Object> list(Map<String, Object> queryParams) {
        return client.get("/api/v1/transactions", queryParams);
    }

    /**
     * Get a transaction by ID.
     */
    public ApiResponse<Object> get(String id) {
        return client.get("/api/v1/transactions/" + id);
    }

    /**
     * Search transactions.
     */
    public ApiResponse<Object> search(Map<String, Object> queryParams) {
        return client.get("/api/v1/transactions/search", queryParams);
    }
}
