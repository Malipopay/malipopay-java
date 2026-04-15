package co.tz.malipopay.webhooks;

import co.tz.malipopay.exceptions.MalipopayException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Webhook utilities for verifying signatures and constructing event objects.
 */
public class Webhooks {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private final String secret;
    private final Gson gson;

    public Webhooks(String secret) {
        this.secret = secret;
        this.gson = new Gson();
    }

    /**
     * Verify a webhook signature against the raw request body.
     *
     * @param payload   the raw request body string
     * @param signature the signature from the webhook header
     * @return true if the signature is valid
     */
    public boolean verify(String payload, String signature) {
        if (secret == null || secret.isEmpty()) {
            throw new MalipopayException("Webhook secret is not configured");
        }
        if (payload == null || signature == null) {
            return false;
        }

        String computed = computeHmac(payload);
        return MessageDigest.isEqual(
                computed.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Verify the webhook and parse the payload into a map.
     *
     * @param payload   the raw request body string
     * @param signature the signature from the webhook header
     * @return parsed event data as a Map
     * @throws MalipopayException if verification fails
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> constructEvent(String payload, String signature) {
        if (!verify(payload, signature)) {
            throw new MalipopayException("Webhook signature verification failed");
        }
        return gson.fromJson(payload, Map.class);
    }

    /**
     * Compute HMAC-SHA256 hex digest for the given payload.
     */
    private String computeHmac(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec keySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new MalipopayException("Failed to compute HMAC: " + e.getMessage(), e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
