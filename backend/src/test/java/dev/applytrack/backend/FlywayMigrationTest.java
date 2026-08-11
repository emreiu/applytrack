package dev.applytrack.backend;

import dev.applytrack.backend.testsupport.AbstractIntegrationTest;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayMigrationTest extends AbstractIntegrationTest {

    private final Flyway flyway;

    @Autowired
    FlywayMigrationTest(Flyway flyway) {
        this.flyway = flyway;
    }

    @Test
    void allMigrationsAreAppliedOnEmptyDatabase() {
        assertThat(flyway.info().pending()).isEmpty();
    }
}