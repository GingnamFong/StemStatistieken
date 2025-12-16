package nl.hva.ict.sm3.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.Arrays;

@Configuration
public class APIConfig implements WebMvcConfigurer {
    @Value("${cors.allowed-origins:}")
    private String allowed;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = Arrays.stream(allowed.split(","))
                                 .map(String::trim)
                                 .filter(s -> !s.isEmpty())
                                 .toArray(String[]::new);

        if (origins.length > 0) {
            // Use explicit origins when configured
            registry.addMapping("/**")
                    .allowedOrigins(origins)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                    .allowedHeaders("Authorization", "Cache-Control", "Content-Type", "X-Requested-With")
                    .allowCredentials(true)
                    .maxAge(3600); // Cache preflight for 1 hour
        } else {
            // Permissive CORS for development (no explicit origins configured)
            registry.addMapping("/**")
                    .allowedOriginPatterns("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                    .allowedHeaders("*")
                    .allowCredentials(false) // Can't use credentials with wildcard
                    .maxAge(3600);
        }
    }
}
