package nl.hva.ict.sm3.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nl.hva.ict.sm3.backend.model.User;
import nl.hva.ict.sm3.backend.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


/**
 * SimpleTokenAuthFilter is a lightweight authentication filter that
 * authenticates users based on a simple Bearer token.
 *
 * <p>The expected token format is:
 * <ul>
 *   <li>{@code user-{id}}</li>
 *   <li>{@code user-{id}-{timestamp}}</li>
 * </ul>
 *
 * <p>The filter extracts the user ID from the token, retrieves the corresponding
 * {@link User} from the database, and sets the authentication in the
 * {@link SecurityContextHolder}.
 *
 *
 */

public class SimpleTokenAuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    /**
     * Creates a new {@code SimpleTokenAuthFilter}.
     *
     * @param userRepository repository used to load users by ID
     */
    public SimpleTokenAuthFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    /**
     * Processes each incoming HTTP request and checks for a Bearer token
     * in the {@code Authorization} header.
     *
     * <p>If a valid token is found and no authentication is present yet,
     * the user is authenticated and stored in the security context.
     *
     * @param request  the incoming HTTP request
     * @param response the HTTP response
     * @param chain    the remaining filter chain
     * @throws ServletException if the filter fails
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim(); // verwacht: user-123 of user-123-....
            Long userId = parseUserId(token);

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    // principal = email, zodat jouw code auth.getName() -> email blijft werken
                    var auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }

        chain.doFilter(request, response);
    }
    /**
     * Extracts the user ID from a token string.
     *
     * <p>Supports tokens in the following formats:
     * <ul>
     *   <li>{@code user-123}</li>
     *   <li>{@code user-123-<timestamp>}</li>
     * </ul>
     *
     * @param token the raw token value (without "Bearer ")
     * @return the extracted user ID, or {@code null} if the token is invalid
     */
    private Long parseUserId(String token) {
        try {
            if (!token.startsWith("user-")) return null;

            // ondersteunt zowel "user-123" als "user-123-<timestamp>"
            String rest = token.substring(5);
            String idPart = rest.contains("-") ? rest.substring(0, rest.indexOf('-')) : rest;

            return Long.parseLong(idPart);
        } catch (Exception e) {
            return null;
        }
    }
}
