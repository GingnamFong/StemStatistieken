package nl.hva.ict.sm3.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nl.hva.ict.sm3.backend.model.User;
import nl.hva.ict.sm3.backend.repository.UserRepository;
import nl.hva.ict.sm3.backend.service.TokenService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


/**
 * SimpleTokenAuthFilter is an authentication filter that
 * authenticates users based on Bearer tokens with expiration.
 *
 * <p>The expected token format is:
 * <ul>
 *   <li>{@code user-{userId}-{timestamp}}</li>
 * </ul>
 *
 * <p>The filter validates the token expiration, retrieves the corresponding
 * {@link User} from the database, and sets the authentication in the
 * {@link SecurityContextHolder}.
 *
 *
 */

public class SimpleTokenAuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    
    /**
     * Creates a new {@code SimpleTokenAuthFilter}.
     *
     * @param userRepository repository used to load users by ID
     * @param tokenService service for validating tokens
     */
    public SimpleTokenAuthFilter(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
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
            String token = header.substring(7).trim();
            
            // Validate token (signature + expiration check)
            Long userId = tokenService.validateToken(token);

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
}
