package nl.hva.ict.sm3.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * Service for generating and validating secure authentication tokens.
 * 
 * <p>Tokens are formatted as: user-{userId}-{timestamp}-{signature}
 * 
 * <p>Security features:
 * <ul>
 *   <li>HMAC-SHA256 signature to prevent tampering</li>
 *   <li>Expiration time (default 7 days)</li>
 * </ul>
 */
@Service
public class TokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final long TOKEN_EXPIRATION_SECONDS = 7 * 24 * 60 * 60; // 7 days
    private static final String TOKEN_PREFIX = "user-";
    private static final String TOKEN_SEPARATOR = "-";

    private final Mac hmac;

    /**
     * Creates a TokenService with the provided secret key.
     * 
     * @param tokenSecret the secret key for signing tokens (should be at least 32 characters)
     */
    public TokenService(@Value("${app.token.secret}") String tokenSecret) {
        try {
            // Initialize HMAC with the secret key
            SecretKeySpec keySpec = new SecretKeySpec(
                tokenSecret.getBytes(StandardCharsets.UTF_8), 
                HMAC_ALGORITHM
            );
            this.hmac = Mac.getInstance(HMAC_ALGORITHM);
            this.hmac.init(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize TokenService", e);
        }
    }

    /**
     * Generates a secure authentication token for a user.
     * 
     * @param userId the user ID
     * @return a signed token with format: user-{userId}-{timestamp}-{signature}
     */
    public String generateToken(Long userId) {
        long timestamp = Instant.now().getEpochSecond();
        
        // Create payload: userId + timestamp
        String payload = userId + TOKEN_SEPARATOR + timestamp;
        
        // Generate HMAC-SHA256 signature
        String signature = computeSignature(payload);
        
        // Format: user-{userId}-{timestamp}-{signature}
        return TOKEN_PREFIX + payload + TOKEN_SEPARATOR + signature;
    }

    /**
     * Validates a token and extracts the user ID if valid.
     * 
     * @param token the token to validate
     * @return the user ID if token is valid and not expired, null otherwise
     */
    public Long validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        try {
            // Check prefix
            if (!token.startsWith(TOKEN_PREFIX)) {
                return null;
            }

            // Remove prefix
            String tokenBody = token.substring(TOKEN_PREFIX.length());
            
            // Split into parts: userId-timestamp-signature
            String[] parts = tokenBody.split(TOKEN_SEPARATOR, -1);
            if (parts.length < 3) {
                return null;
            }

            // Extract components
            Long userId = Long.parseLong(parts[0]);
            long timestamp = Long.parseLong(parts[1]);
            String providedSignature = parts[2];

            // Verify HMAC-SHA256 signature
            String payload = parts[0] + TOKEN_SEPARATOR + parts[1];
            String expectedSignature = computeSignature(payload);
            
            if (!constantTimeEquals(providedSignature, expectedSignature)) {
                // Signature mismatch - token has been tampered with
                return null;
            }

            // Check expiration
            long currentTime = Instant.now().getEpochSecond();
            long tokenAge = currentTime - timestamp;
            
            if (tokenAge < 0) {
                // Token from the future (clock skew) - reject
                return null;
            }
            
            if (tokenAge > TOKEN_EXPIRATION_SECONDS) {
                // Token expired
                return null;
            }

            return userId;

        } catch (Exception e) {
            // Any parsing or validation error invalidates the token
            return null;
        }
    }

    /**
     * Computes HMAC-SHA256 signature for a payload.
     */
    private String computeSignature(String payload) {
        try {
            Mac mac = (Mac) this.hmac.clone();
            byte[] hashBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            // Use URL-safe base64 encoding (no padding for shorter tokens)
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute token signature", e);
        }
    }

    /**
     * Constant-time string comparison to prevent timing attacks.
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}

