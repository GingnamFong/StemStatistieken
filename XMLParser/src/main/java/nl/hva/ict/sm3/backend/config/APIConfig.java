package nl.hva.ict.sm3.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class APIConfig implements WebMvcConfigurer {
    @Value("${election.frontend.url}")
    private String frontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        System.out.println("Adding CORS mappings");
        System.out.println("frontendUrl: " + frontendUrl);
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        frontendUrl,
                        "http://my.perfect.app.nl"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
