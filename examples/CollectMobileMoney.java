import co.tz.malipopay.ApiResponse;
import co.tz.malipopay.Malipopay;
import co.tz.malipopay.MalipopayConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Example: Collect mobile money payment via Malipopay.
 */
public class CollectMobileMoney {

    public static void main(String[] args) {
        // Initialize the client (defaults to production)
        Malipopay client = new Malipopay("your-api-key");

        // Or use UAT environment for testing
        // MalipopayConfig config = new MalipopayConfig.Builder()
        //         .environment(MalipopayConfig.Environment.UAT)
        //         .build();
        // Malipopay client = new Malipopay("your-api-key", config);

        // Build collection parameters
        Map<String, Object> params = new HashMap<>();
        params.put("amount", 5000);
        params.put("phone", "255712345678");
        params.put("provider", "Vodacom");
        params.put("reference", "ORDER-001");
        params.put("description", "Payment for order #001");

        try {
            // Collect payment
            ApiResponse<Object> response = client.payments().collect(params);

            if (response.isSuccess()) {
                System.out.println("Collection initiated successfully!");
                System.out.println("Data: " + response.getData());
            } else {
                System.out.println("Collection failed: " + response.getMessage());
            }

            // Verify payment status
            ApiResponse<Object> verification = client.payments().verify("ORDER-001");
            System.out.println("Payment status: " + verification.getData());

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
