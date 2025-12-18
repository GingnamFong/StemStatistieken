package nl.hva.ict.sm3.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.Arrays;

// Disabled - using CorsConfig instead to avoid conflicts
// @Configuration
public class APIConfig implements WebMvcConfigurer {
    @Value("${cors.allowed-origins:}")
    private String allowed;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = Arrays.stream(allowed.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        registry.addMapping("/**")
                // Use allowedOriginPatterns to support both exact origins and patterns
                // This works with credentials and is more flexible
                .allowedOriginPatterns(origins.length > 0 ? origins : new String[] {"*"})
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS","PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
