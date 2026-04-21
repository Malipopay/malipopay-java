package tz.co.malipopay;

import tz.co.malipopay.resources.*;
import tz.co.malipopay.webhooks.Webhooks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MalipopayTest {

    @Test
    void shouldCreateClientWithApiKey() {
        Malipopay client = new Malipopay("test-api-key");
        assertEquals("test-api-key", client.getApiKey());
        assertNotNull(client.getConfig());
    }

    @Test
    void shouldThrowOnNullApiKey() {
        assertThrows(IllegalArgumentException.class, () -> new Malipopay(null));
    }

    @Test
    void shouldThrowOnEmptyApiKey() {
        assertThrows(IllegalArgumentException.class, () -> new Malipopay(""));
        assertThrows(IllegalArgumentException.class, () -> new Malipopay("   "));
    }

    @Test
    void shouldCreateClientWithCustomConfig() {
        MalipopayConfig config = new MalipopayConfig.Builder()
                .environment(MalipopayConfig.Environment.UAT)
                .timeout(60000)
                .retries(5)
                .webhookSecret("whsec_test")
                .build();

        Malipopay client = new Malipopay("test-api-key", config);
        assertEquals(MalipopayConfig.Environment.UAT, client.getConfig().getEnvironment());
        assertEquals("https://core-uat.malipopay.co.tz", client.getConfig().getBaseUrl());
        assertEquals(60000, client.getConfig().getTimeout());
        assertEquals(5, client.getConfig().getRetries());
    }

    @Test
    void shouldCreateClientWithCustomBaseUrl() {
        MalipopayConfig config = new MalipopayConfig.Builder()
                .baseUrl("https://custom.example.com")
                .build();

        Malipopay client = new Malipopay("test-api-key", config);
        assertEquals("https://custom.example.com", client.getConfig().getBaseUrl());
    }

    @Test
    void shouldLazyInitResources() {
        Malipopay client = new Malipopay("test-api-key");

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
        Malipopay client = new Malipopay("test-api-key");

        Payments p1 = client.payments();
        Payments p2 = client.payments();
        assertSame(p1, p2, "payments() should return the same instance");
    }

    @Test
    void shouldInitWebhooksWithSecret() {
        MalipopayConfig config = new MalipopayConfig.Builder()
                .webhookSecret("whsec_test_secret")
                .build();
        Malipopay client = new Malipopay("test-api-key", config);

        Webhooks wh = client.webhooks();
        assertNotNull(wh);
    }

    @Test
    void shouldDefaultToProductionEnvironment() {
        MalipopayConfig config = MalipopayConfig.defaults();
        assertEquals(MalipopayConfig.Environment.PRODUCTION, config.getEnvironment());
        assertEquals("https://core-prod.malipopay.co.tz", config.getBaseUrl());
    }
}
