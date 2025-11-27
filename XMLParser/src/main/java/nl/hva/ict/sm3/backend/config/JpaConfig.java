package nl.hva.ict.sm3.backend.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class JpaConfig {
    // Use Spring Boot's auto-configured DataSource and EntityManagerFactory.
    // No manual beans here to avoid driver and ordering issues.
}
