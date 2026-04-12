package co.tz.malipopay;

import co.tz.malipopay.resources.*;
import co.tz.malipopay.webhooks.Webhooks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaliPoPayTest {

    @Test
    void shouldCreateClientWithApiKey() {
        MaliPoPay client = new MaliPoPay("test-api-key");
        assertEquals("test-api-key", client.getApiKey());
        assertNotNull(client.getConfig());
    }

    @Test
    void shouldThrowOnNullApiKey() {
        assertThrows(IllegalArgumentException.class, () -> new MaliPoPay(null));
    }

    @Test
    void shouldThrowOnEmptyApiKey() {
        assertThrows(IllegalArgumentException.class, () -> new MaliPoPay(""));
        assertThrows(IllegalArgumentException.class, () -> new MaliPoPay("   "));
    }

    @Test
    void shouldCreateClientWithCustomConfig() {
        MaliPoPayConfig config = new MaliPoPayConfig.Builder()
                .environment(MaliPoPayConfig.Environment.UAT)
                .timeout(60000)
                .retries(5)
                .webhookSecret("whsec_test")
                .build();

        MaliPoPay client = new MaliPoPay("test-api-key", config);
        assertEquals(MaliPoPayConfig.Environment.UAT, client.getConfig().getEnvironment());
        assertEquals("https://core-uat.malipopay.co.tz", client.getConfig().getBaseUrl());
        assertEquals(60000, client.getConfig().getTimeout());
        assertEquals(5, client.getConfig().getRetries());
    }

    @Test
    void shouldCreateClientWithCustomBaseUrl() {
        MaliPoPayConfig config = new MaliPoPayConfig.Builder()
                .baseUrl("https://custom.example.com")
                .build();

        MaliPoPay client = new MaliPoPay("test-api-key", config);
        assertEquals("https://custom.example.com", client.getConfig().getBaseUrl());
    }

    @Test
    void shouldLazyInitResources() {
        MaliPoPay client = new MaliPoPay("test-api-key");

        // Each accessor should return a non-null instance
        assertNotNull(client.payments());
        assertNotNull(client.customers());
        assertNotNull(client.invoices());
        assertNotNull(client.products());
        assertNotNull(client.transactions());
        assertNotNull(client.account());
        assertNotNull(client.sms());
        assertNotNull(client.references());
    }

    @Test
    void shouldReturnSameResourceInstance() {
        MaliPoPay client = new MaliPoPay("test-api-key");

        Payments p1 = client.payments();
        Payments p2 = client.payments();
        assertSame(p1, p2, "payments() should return the same instance");
    }

    @Test
    void shouldInitWebhooksWithSecret() {
        MaliPoPayConfig config = new MaliPoPayConfig.Builder()
                .webhookSecret("whsec_test_secret")
                .build();
        MaliPoPay client = new MaliPoPay("test-api-key", config);

        Webhooks wh = client.webhooks();
        assertNotNull(wh);
    }

    @Test
    void shouldDefaultToProductionEnvironment() {
        MaliPoPayConfig config = MaliPoPayConfig.defaults();
        assertEquals(MaliPoPayConfig.Environment.PRODUCTION, config.getEnvironment());
        assertEquals("https://core-prod.malipopay.co.tz", config.getBaseUrl());
    }
}
