package ru.ac.uniyar.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.ktorm.database.Database
import org.ktorm.logging.ConsoleLogger
import org.ktorm.logging.LogLevel
import org.ktorm.support.mysql.MySqlDialect

class DbConfig {
    companion object {

        private val config = HikariConfig().apply {
            jdbcUrl = H2DatabaseManager.JDBC_CONNECTION
            driverClassName = "org.h2.Driver"
            maximumPoolSize = 10
        }
        private val dataSource = HikariDataSource(config)
        fun getConnection(): Database {
            return Database.connect(
                dataSource = dataSource,
                dialect = MySqlDialect(),
                logger = ConsoleLogger(threshold = LogLevel.DEBUG)
            )
        }
    }
}
