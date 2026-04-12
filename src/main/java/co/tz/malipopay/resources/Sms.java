package co.tz.malipopay.resources;

import co.tz.malipopay.ApiResponse;
import co.tz.malipopay.HttpClient;

import java.util.Map;

/**
 * SMS resource — send single, bulk, and scheduled SMS messages.
 */
public class Sms {

    private final HttpClient client;

    public Sms(HttpClient client) {
        this.client = client;
    }

    /**
     * Send a single SMS.
     */
    public ApiResponse<Object> send(Map<String, Object> params) {
        return client.post("/sms/", params);
    }

    /**
     * Send bulk SMS.
     */
    public ApiResponse<Object> sendBulk(Map<String, Object> params) {
        return client.post("/sms/bulk", params);
    }

    /**
     * Schedule an SMS for later delivery.
     */
    public ApiResponse<Object> schedule(Map<String, Object> params) {
        return client.post("/sms/schedule", params);
    }
}
