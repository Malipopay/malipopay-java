package tz.co.malipopay;

/**
 * Configuration for the Malipopay SDK client.
 * Use the {@link Builder} for a fluent construction pattern.
 */
public class MalipopayConfig {

    public enum Environment {
        PRODUCTION("https://core-prod.malipopay.co.tz"),
        UAT("https://core-uat.malipopay.co.tz");

        private final String baseUrl;

        Environment(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getBaseUrl() {
            return baseUrl;
        }
    }

    private final Environment environment;
    private final String baseUrl;
    private final int timeout;
    private final int retries;
    private final String webhookSecret;

    private MalipopayConfig(Builder builder) {
        this.environment = builder.environment;
        this.baseUrl = builder.baseUrl != null ? builder.baseUrl : builder.environment.getBaseUrl();
        this.timeout = builder.timeout;
        this.retries = builder.retries;
        this.webhookSecret = builder.webhookSecret;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getRetries() {
        return retries;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    /**
     * Creates a default configuration pointing to production.
     */
    public static MalipopayConfig defaults() {
        return new Builder().build();
    }

    public static class Builder {
        private Environment environment = Environment.PRODUCTION;
        private String baseUrl = null;
        private int timeout = 30000;
        private int retries = 3;
        private String webhookSecret = null;

        public Builder environment(Environment environment) {
            this.environment = environment;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder retries(int retries) {
            this.retries = retries;
            return this;
        }

        public Builder webhookSecret(String webhookSecret) {
            this.webhookSecret = webhookSecret;
            return this;
        }

        public MalipopayConfig build() {
            return new MalipopayConfig(this);
        }
    }
}
