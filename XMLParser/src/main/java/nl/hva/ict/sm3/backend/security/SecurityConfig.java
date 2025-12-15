package nl.hva.ict.sm3.backend.security;

import nl.hva.ict.sm3.backend.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, UserRepository userRepository) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Auth endpoints open
                        .requestMatchers("/api/auth/**").permitAll()

                        // Alles lezen open (teamgenoot veilig)
                        .requestMatchers(HttpMethod.GET, "/api/forum/**").permitAll()

                        // Jouw write endpoints beveiligd
                        .requestMatchers(HttpMethod.POST, "/api/forum/*/comments").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/forum/comments/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/forum/comments/*/like").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/forum/comments/*/like").authenticated()

                        // Rest open laten zodat je niks per ongeluk breekt
                        .anyRequest().permitAll()
                )
                .addFilterBefore(new SimpleTokenAuthFilter(userRepository), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
