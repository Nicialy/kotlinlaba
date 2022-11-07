package ru.ac.uniyar.database

import org.flywaydb.core.Flyway;

fun performMigrations() {
    val flyway = Flyway
        .configure()
        .locations("ru/ac/uniyar/models/db/migrations")
        .validateMigrationNaming(true)
        .dataSource(H2DatabaseManager.JDBC_CONNECTION, null, null)
        .load()
    flyway.migrate()
}