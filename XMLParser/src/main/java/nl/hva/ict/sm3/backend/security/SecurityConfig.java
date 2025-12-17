package nl.hva.ict.sm3.backend.security;

import nl.hva.ict.sm3.backend.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;


/**
 * Security configuration for the backend application.
 *
 * <p>This configuration:
 * <ul>
 *   <li>Disables session-based authentication (stateless API)</li>
 *   <li>Allows access to public endpoints such as authentication and forum reads</li>
 *   <li>Protects write operations on forum comments and likes</li>
 *   <li>Enables access to the H2 console for development purposes</li>
 * </ul>
 *
 * <p>A custom {@link SimpleTokenAuthFilter} is used to authenticate users
 * based on a simple Bearer token.
 *
 */
@Configuration
public class SecurityConfig {

    /**
     * Configures the Spring Security filter chain.
     *
     * <p>The configuration ensures:
     * <ul>
     *   <li>CSRF protection is disabled for the H2 console</li>
     *   <li>Frame options are configured to allow the H2 console to render</li>
     *   <li>Stateless session management</li>
     *   <li>Role-free, token-based access control for selected endpoints</li>
     * </ul>
     *
     * @param http            the {@link HttpSecurity} to configure
     * @param userRepository  repository used by the authentication filter
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the security configuration fails
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, UserRepository userRepository) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure endpoint access rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        // Authentication endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Public forum read access
                        .requestMatchers(HttpMethod.GET, "/api/forum/**").permitAll()

                        // Protected forum write actions
                        .requestMatchers(HttpMethod.POST, "/api/forum/questions").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/forum/*/questions").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/forum/questions/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/forum/questions").authenticated()

                        .anyRequest().permitAll()
                )
                .addFilterBefore(new SimpleTokenAuthFilter(userRepository), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
