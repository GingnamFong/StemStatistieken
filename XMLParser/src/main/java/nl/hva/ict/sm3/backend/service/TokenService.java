package nl.hva.ict.sm3.backend.service;

import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service for generating and validating authentication tokens with expiration.
 * 
 * <p>Tokens are formatted as: user-{userId}-{timestamp}
 * 
 * <p>Security features:
 * <ul>
 *   <li>Expiration time (default 7 days)</li>
 * </ul>
 */
@Service
public class TokenService {

    private static final long TOKEN_EXPIRATION_SECONDS = 7 * 24 * 60 * 60; // 7 days
    private static final String TOKEN_PREFIX = "user-";
    private static final String TOKEN_SEPARATOR = "-";

    /**
     * Creates a TokenService.
     */
    public TokenService() {
        // No initialization needed
    }

    /**
     * Generates an authentication token for a user.
     * 
     * @param userId the user ID
     * @return a token with format: user-{userId}-{timestamp}
     */
    public String generateToken(Long userId) {
        long timestamp = Instant.now().getEpochSecond();
        return TOKEN_PREFIX + userId + TOKEN_SEPARATOR + timestamp;
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
            
            // Split into parts: userId-timestamp
            String[] parts = tokenBody.split(TOKEN_SEPARATOR, -1);
            if (parts.length < 2) {
                return null;
            }

            // Extract components
            Long userId = Long.parseLong(parts[0]);
            long timestamp = Long.parseLong(parts[1]);

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
}

