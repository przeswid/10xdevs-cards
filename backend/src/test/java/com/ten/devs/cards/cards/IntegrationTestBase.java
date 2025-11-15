package com.ten.devs.cards.cards;

import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests using Testcontainers.
 *
 * This provides:
 * - PostgreSQL container for realistic database testing
 * - Spring Boot application context
 * - Test profile activation
 * - Automatic database configuration
 *
 * Usage:
 * <pre>
 * {@code
 * @SpringBootTest
 * class MyIntegrationTest extends IntegrationTestBase {
 *
 *     @Autowired
 *     private MyRepository repository;
 *
 *     @Test
 *     void testDatabaseOperation() {
 *         // Test implementation
 *     }
 * }
 * }
 * </pre>
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IntegrationTestBase {

    /**
     * PostgreSQL container shared across all integration tests.
     * Using PostgreSQL 15 to match production environment.
     */
    @Container
    protected static final PostgreSQLContainer<?> postgreSQLContainer;

    static {
        postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);
        postgreSQLContainer.start();
    }

    /**
     * Dynamically configure Spring datasource properties from Testcontainers.
     */
    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }
}
