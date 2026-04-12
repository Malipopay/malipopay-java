import co.tz.malipopay.ApiResponse;
import co.tz.malipopay.MaliPoPay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Example: Create and manage invoices via MaliPoPay.
 */
public class CreateInvoice {

    public static void main(String[] args) {
        MaliPoPay client = new MaliPoPay("your-api-key");

        try {
            // Create a customer first
            Map<String, Object> customerParams = new HashMap<>();
            customerParams.put("name", "John Doe");
            customerParams.put("email", "john@example.com");
            customerParams.put("phone", "255712345678");

            ApiResponse<Object> customerResponse = client.customers().create(customerParams);
            System.out.println("Customer created: " + customerResponse.getData());

            // Create an invoice
            Map<String, Object> item = new HashMap<>();
            item.put("name", "Web Development");
            item.put("quantity", 1);
            item.put("price", 500000);

            List<Map<String, Object>> items = new ArrayList<>();
            items.add(item);

            Map<String, Object> invoiceParams = new HashMap<>();
            invoiceParams.put("customerId", "customer-id-here");
            invoiceParams.put("items", items);
            invoiceParams.put("currency", "TZS");
            invoiceParams.put("dueDate", "2026-05-01");

            ApiResponse<Object> invoiceResponse = client.invoices().create(invoiceParams);
            System.out.println("Invoice created: " + invoiceResponse.getData());

            // List all invoices
            ApiResponse<Object> invoices = client.invoices().list();
            System.out.println("All invoices: " + invoices.getData());

            // Record a payment against the invoice
            Map<String, Object> paymentParams = new HashMap<>();
            paymentParams.put("invoiceId", "invoice-id-here");
            paymentParams.put("amount", 500000);
            paymentParams.put("method", "mobile_money");

            ApiResponse<Object> paymentResponse = client.invoices().recordPayment(paymentParams);
            System.out.println("Payment recorded: " + paymentResponse.getData());

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
