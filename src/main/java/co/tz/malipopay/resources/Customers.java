package co.tz.malipopay.resources;

import co.tz.malipopay.ApiResponse;
import co.tz.malipopay.HttpClient;

import java.util.Map;

/**
 * Customer resource — create, list, get, search, and verify customers.
 */
public class Customers {

    private final HttpClient client;

    public Customers(HttpClient client) {
        this.client = client;
    }

    /**
     * Create a new customer.
     */
    public ApiResponse<Object> create(Map<String, Object> params) {
        return client.post("/api/v1/customer", params);
    }

    /**
     * List customers.
     */
    public ApiResponse<Object> list() {
        return client.get("/api/v1/customer");
    }

    /**
     * List customers with query parameters.
     */
    public ApiResponse<Object> list(Map<String, Object> queryParams) {
        return client.get("/api/v1/customer", queryParams);
    }

    /**
     * Get a customer by ID.
     */
    public ApiResponse<Object> get(String id) {
        return client.get("/api/v1/customer/" + id);
    }

    /**
     * Search customers.
     */
    public ApiResponse<Object> search(Map<String, Object> queryParams) {
        return client.get("/api/v1/customer/search", queryParams);
    }

    /**
     * Verify a customer.
     */
    public ApiResponse<Object> verify(Map<String, Object> params) {
        return client.post("/api/v1/customer/verify", params);
    }
}
