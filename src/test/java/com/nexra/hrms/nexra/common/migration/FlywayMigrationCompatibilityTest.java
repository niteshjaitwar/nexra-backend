package com.nexra.hrms.nexra.common.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

/**
 * Validates launch-gate migration paths:
 * 1) clean database to latest schema
 * 2) previous release schema (v42) to latest schema
 */
class FlywayMigrationCompatibilityTest {

    private static final String H2_DRIVER = "org.h2.Driver";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    @Test
    void shouldMigrateCleanDatabaseToLatest() {
        Flyway flyway = flyway("jdbc:h2:mem:nexra_migration_clean;MODE=MySQL;DB_CLOSE_DELAY=-1");
        flyway.clean();
        flyway.migrate();

        var current = flyway.info().current();
        assertNotNull(current, "Flyway current version should exist after migration");
        assertEquals("62", current.getVersion().getVersion(),
            "Latest schema version should be v62");
    }

    @Test
    void shouldUpgradeFromPreviousReleaseV42ToLatest() {
        String url = "jdbc:h2:mem:nexra_migration_prev;MODE=MySQL;DB_CLOSE_DELAY=-1";
        Flyway baseline = flyway(url);
        baseline.clean();
        flyway(url, "42").migrate();

        Flyway upgrade = flyway(url);
        upgrade.migrate();

        var current = upgrade.info().current();
        assertNotNull(current, "Flyway current version should exist after upgrade");
        assertEquals("62", current.getVersion().getVersion(),
            "Upgrade path from v42 should land on v62");
    }

    private Flyway flyway(final String url) {
        return flyway(url, null);
    }

    private Flyway flyway(final String url, final String targetVersion) {
        var config = Flyway.configure()
            .locations("classpath:db/migration")
            .cleanDisabled(false)
            .driver(H2_DRIVER)
            .dataSource(url, USER, PASSWORD);
        if (targetVersion != null) {
            config.target(targetVersion);
        }
        return config.load();
    }

}
