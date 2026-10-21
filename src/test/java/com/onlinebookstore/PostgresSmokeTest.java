package com.onlinebookstore;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

/**
 * Verifies the Flyway migrations and JPA validation against a real PostgreSQL
 * server (embedded binaries, no Docker required).
 */
@SpringBootTest
class PostgresSmokeTest {

    static final EmbeddedPostgres POSTGRES = startEmbedded();

    private static EmbeddedPostgres startEmbedded() {
        try {
            return EmbeddedPostgres.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @AfterAll
    static void stopEmbedded() throws IOException {
        POSTGRES.close();
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void migrationsAppliedAndSeedLoaded() {
        Integer bookCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books", Integer.class);
        assertThat(bookCount).isEqualTo(15);

        Integer userTables = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name IN ('users', 'books', 'orders', 'order_items')",
                Integer.class);
        assertThat(userTables).isEqualTo(4);
    }
}
