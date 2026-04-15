package co.tz.malipopay.resources;

import co.tz.malipopay.ApiResponse;
import co.tz.malipopay.HttpClient;

/**
 * Reference data resource — banks, currencies, countries, and institutions.
 */
public class References {

    private final HttpClient client;

    public References(HttpClient client) {
        this.client = client;
    }

    /**
     * List supported banks.
     */
    public ApiResponse<Object> banks() {
        return client.get("/api/v1/standard/banks");
    }

    /**
     * List supported currencies.
     */
    public ApiResponse<Object> currencies() {
        return client.get("/api/v1/standard/currency");
    }

    /**
     * List supported countries.
     */
    public ApiResponse<Object> countries() {
        return client.get("/api/v1/standard/countries");
    }

    /**
     * List supported institutions.
     */
    public ApiResponse<Object> institutions() {
        return client.get("/api/v1/standard/institutions");
    }
}
