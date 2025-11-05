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

        registry.addMapping("/**")
                // use allowedOrigins if explicit, otherwise a permissive pattern for dev
                .allowedOrigins(origins.length > 0 ? origins : new String[] {})
                .allowedOriginPatterns(origins.length == 0 ? new String[] {"*"} : new String[] {})
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
                .allowedHeaders("Authorization","Cache-Control","Content-Type")
                .allowCredentials(origins.length > 0); // Only allow set origin
    }
}
