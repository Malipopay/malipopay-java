package tz.co.malipopay.resources;

import tz.co.malipopay.ApiResponse;
import tz.co.malipopay.HttpClient;

import java.util.Map;

/**
 * Product resource — create, list, and get products.
 */
public class Products {

    private final HttpClient client;

    public Products(HttpClient client) {
        this.client = client;
    }

    /**
     * Create a new product.
     */
    public ApiResponse<Object> create(Map<String, Object> params) {
        return client.post("/api/v1/product", params);
    }

    /**
     * List products.
     */
    public ApiResponse<Object> list() {
        return client.get("/api/v1/product");
    }

    /**
     * List products with query parameters.
     */
    public ApiResponse<Object> list(Map<String, Object> queryParams) {
        return client.get("/api/v1/product", queryParams);
    }

    /**
     * Get a product by ID.
     */
    public ApiResponse<Object> get(String id) {
        return client.get("/api/v1/product/" + id);
    }
}
