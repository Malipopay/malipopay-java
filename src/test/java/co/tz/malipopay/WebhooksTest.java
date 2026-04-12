package co.tz.malipopay;

import co.tz.malipopay.exceptions.MaliPoPayException;
import co.tz.malipopay.webhooks.Webhooks;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WebhooksTest {

    private static final String SECRET = "whsec_test_secret_123";

    @Test
    void shouldVerifyValidSignature() throws Exception {
        Webhooks webhooks = new Webhooks(SECRET);
        String payload = "{\"event\":\"payment.completed\",\"data\":{\"id\":\"123\"}}";
        String signature = computeHmac(SECRET, payload);

        assertTrue(webhooks.verify(payload, signature));
    }

    @Test
    void shouldRejectInvalidSignature() {
        Webhooks webhooks = new Webhooks(SECRET);
        String payload = "{\"event\":\"payment.completed\",\"data\":{\"id\":\"123\"}}";

        assertFalse(webhooks.verify(payload, "invalid_signature"));
    }

    @Test
    void shouldRejectTamperedPayload() throws Exception {
        Webhooks webhooks = new Webhooks(SECRET);
        String originalPayload = "{\"event\":\"payment.completed\",\"data\":{\"id\":\"123\"}}";
        String signature = computeHmac(SECRET, originalPayload);

        String tamperedPayload = "{\"event\":\"payment.completed\",\"data\":{\"id\":\"999\"}}";
        assertFalse(webhooks.verify(tamperedPayload, signature));
    }

    @Test
    void shouldConstructEventFromValidPayload() throws Exception {
        Webhooks webhooks = new Webhooks(SECRET);
        String payload = "{\"event\":\"payment.completed\",\"data\":{\"id\":\"123\"}}";
        String signature = computeHmac(SECRET, payload);

        Map<String, Object> event = webhooks.constructEvent(payload, signature);
        assertNotNull(event);
        assertEquals("payment.completed", event.get("event"));
    }

    @Test
    void shouldThrowOnInvalidSignatureInConstructEvent() throws Exception {
        Webhooks webhooks = new Webhooks(SECRET);
        String payload = "{\"event\":\"payment.completed\"}";

        assertThrows(MaliPoPayException.class, () ->
                webhooks.constructEvent(payload, "bad_sig"));
    }

    @Test
    void shouldThrowWhenSecretNotConfigured() {
        Webhooks webhooks = new Webhooks(null);
        assertThrows(MaliPoPayException.class, () ->
                webhooks.verify("{}", "sig"));
    }

    @Test
    void shouldReturnFalseForNullInputs() {
        Webhooks webhooks = new Webhooks(SECRET);
        assertFalse(webhooks.verify(null, "sig"));
        assertFalse(webhooks.verify("{}", null));
    }

    // Helper to compute HMAC-SHA256 hex digest
    private static String computeHmac(String secret, String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
