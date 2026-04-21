package tz.co.malipopay.resources;

import tz.co.malipopay.ApiResponse;
import tz.co.malipopay.HttpClient;

import java.util.Map;

/**
 * Account resource — view account transactions and reconciliation data.
 */
public class Account {

    private final HttpClient client;

    public Account(HttpClient client) {
        this.client = client;
    }

    /**
     * Get all account transactions.
     */
    public ApiResponse<Object> transactions() {
        return client.get("/api/v1/account/allTransaction");
    }

    /**
     * Get account transactions with query parameters.
     */
    public ApiResponse<Object> transactions(Map<String, Object> queryParams) {
        return client.get("/api/v1/account/allTransaction", queryParams);
    }

    /**
     * Get account reconciliation data.
     */
    public ApiResponse<Object> reconciliation() {
        return client.get("/api/v1/account/reconciliation");
    }

    /**
     * Get account reconciliation data with query parameters.
     */
    public ApiResponse<Object> reconciliation(Map<String, Object> queryParams) {
        return client.get("/api/v1/account/reconciliation", queryParams);
    }
}
