import co.tz.malipopay.ApiResponse;
import co.tz.malipopay.MaliPoPay;
import co.tz.malipopay.MaliPoPayConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Example: Collect mobile money payment via MaliPoPay.
 */
public class CollectMobileMoney {

    public static void main(String[] args) {
        // Initialize the client (defaults to production)
        MaliPoPay client = new MaliPoPay("your-api-key");

        // Or use UAT environment for testing
        // MaliPoPayConfig config = new MaliPoPayConfig.Builder()
        //         .environment(MaliPoPayConfig.Environment.UAT)
        //         .build();
        // MaliPoPay client = new MaliPoPay("your-api-key", config);

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
