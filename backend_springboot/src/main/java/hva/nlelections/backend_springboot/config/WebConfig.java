package hva.nlelections.backend_springboot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:}")
    private String allowedOriginsProperty;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> allowedOrigins = parseAllowedOrigins(allowedOriginsProperty);

        if (!allowedOrigins.isEmpty()) {
            registry.addMapping("/**")
                    .allowedOrigins(allowedOrigins.toArray(new String[0]))
                    .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                    .allowedHeaders("Authorization", "Cache-Control", "Content-Type")
                    .allowCredentials(true);
        } else {
            // Default: allow everything (useful for local dev)
            registry.addMapping("/**")
                    .allowedOriginPatterns("*")
                    .allowedMethods("*")
                    .allowedHeaders("*")
                    .allowCredentials(false);
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Explicitly configure resource handlers to only handle specific paths
        // This prevents /api/** from being treated as static resources
        // By default, Spring Boot would try to serve /api/** as static files
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/public/**")
                .addResourceLocations("classpath:/public/");
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("classpath:/resources/");
        // Note: We don't register a catch-all handler, so /api/** will be handled by controllers
    }

    private List<String> parseAllowedOrigins(String property) {
        if (property == null || property.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(property.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
