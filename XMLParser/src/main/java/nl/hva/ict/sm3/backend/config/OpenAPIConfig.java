package nl.hva.ict.sm3.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for API documentation.
 * Provides Swagger UI documentation for all REST endpoints.
 */
@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Election API")
                        .version("1.0.0")
                        .description("API for retrieving election data, provinces, and stemwijzer functionality"));
    }
}
