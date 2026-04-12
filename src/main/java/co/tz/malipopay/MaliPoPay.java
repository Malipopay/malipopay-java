package co.tz.malipopay;

import co.tz.malipopay.resources.*;
import co.tz.malipopay.webhooks.Webhooks;

/**
 * Main entry point for the MaliPoPay Java SDK.
 *
 * <pre>{@code
 * MaliPoPay client = new MaliPoPay("your-api-key");
 * ApiResponse<Object> response = client.payments().collect(Map.of(
 *     "amount", 5000,
 *     "phone", "255712345678",
 *     "provider", "Vodacom"
 * ));
 * }</pre>
 */
public class MaliPoPay {

    private final String apiKey;
    private final MaliPoPayConfig config;
    private final HttpClient httpClient;

    // Lazy-initialized resource instances
    private volatile Payments payments;
    private volatile Customers customers;
    private volatile Invoices invoices;
    private volatile Products products;
    private volatile Transactions transactions;
    private volatile Account account;
    private volatile Sms sms;
    private volatile References references;
    private volatile Webhooks webhooks;

    /**
     * Create a client with the given API key and default production configuration.
     *
     * @param apiKey your MaliPoPay API token
     */
    public MaliPoPay(String apiKey) {
        this(apiKey, MaliPoPayConfig.defaults());
    }

    /**
     * Create a client with the given API key and custom configuration.
     *
     * @param apiKey your MaliPoPay API token
     * @param config custom configuration
     */
    public MaliPoPay(String apiKey, MaliPoPayConfig config) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key is required");
        }
        this.apiKey = apiKey;
        this.config = config;
        this.httpClient = new HttpClient(apiKey, config);
    }

    // ---- Resource accessors (lazy init) ----

    public Payments payments() {
        if (payments == null) {
            synchronized (this) {
                if (payments == null) {
                    payments = new Payments(httpClient);
                }
            }
        }
        return payments;
    }

    public Customers customers() {
        if (customers == null) {
            synchronized (this) {
                if (customers == null) {
                    customers = new Customers(httpClient);
                }
            }
        }
        return customers;
    }

    public Invoices invoices() {
        if (invoices == null) {
            synchronized (this) {
                if (invoices == null) {
                    invoices = new Invoices(httpClient);
                }
            }
        }
        return invoices;
    }

    public Products products() {
        if (products == null) {
            synchronized (this) {
                if (products == null) {
                    products = new Products(httpClient);
                }
            }
        }
        return products;
    }

    public Transactions transactions() {
        if (transactions == null) {
            synchronized (this) {
                if (transactions == null) {
                    transactions = new Transactions(httpClient);
                }
            }
        }
        return transactions;
    }

    public Account account() {
        if (account == null) {
            synchronized (this) {
                if (account == null) {
                    account = new Account(httpClient);
                }
            }
        }
        return account;
    }

    public Sms sms() {
        if (sms == null) {
            synchronized (this) {
                if (sms == null) {
                    sms = new Sms(httpClient);
                }
            }
        }
        return sms;
    }

    public References references() {
        if (references == null) {
            synchronized (this) {
                if (references == null) {
                    references = new References(httpClient);
                }
            }
        }
        return references;
    }

    public Webhooks webhooks() {
        if (webhooks == null) {
            synchronized (this) {
                if (webhooks == null) {
                    webhooks = new Webhooks(config.getWebhookSecret());
                }
            }
        }
        return webhooks;
    }

    /**
     * Returns the API key this client was created with.
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Returns the configuration this client is using.
     */
    public MaliPoPayConfig getConfig() {
        return config;
    }
}
