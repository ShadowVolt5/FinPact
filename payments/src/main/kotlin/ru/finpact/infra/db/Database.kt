package ru.finpact.infra.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.flywaydb.core.Flyway
import java.sql.Connection

object Database {
    private lateinit var dataSource: HikariDataSource

    fun init(config: ApplicationConfig) {
        val db = config.config("db")

        val driver = env("DB_DRIVER") ?: "org.postgresql.Driver"
        val user = envRequired("DB_USER")
        val password = envRequired("DB_PASSWORD")
        val schema = env("DB_SCHEMA") ?: "public"

        val baseJdbcUrl = env("DB_JDBC_URL") ?: run {
            val host = envRequired("DB_HOST")
            val port = env("DB_PORT") ?: "5432"
            val name = envRequired("DB_NAME")
            "jdbc:postgresql://$host:$port/$name"
        }

        val jdbcUrl = when {
            baseJdbcUrl.contains("currentSchema=") -> baseJdbcUrl
            baseJdbcUrl.contains("?") -> "$baseJdbcUrl&currentSchema=$schema"
            else -> "$baseJdbcUrl?currentSchema=$schema"
        }

        Flyway.configure()
            .dataSource(jdbcUrl, user, password)
            .locations("classpath:db/migration")
            .defaultSchema(schema)
            .schemas(schema)
            .createSchemas(true)
            .table("flyway_schema_history_$schema")
            .baselineOnMigrate(true)
            .load()
            .migrate()

        val hc = HikariConfig().apply {
            driverClassName = driver
            this.jdbcUrl = jdbcUrl
            username = user
            this.password = password

            poolName = "PaymentsHikariPool"

            isAutoCommit = db.propertyOrNull("autoCommit")?.getString()?.toBooleanStrictOrNull() ?: false
            transactionIsolation = db.propertyOrNull("transactionIsolation")?.getString()
                ?: "TRANSACTION_READ_COMMITTED"

            maximumPoolSize = db.intOrDefault("maximumPoolSize", 32)
            minimumIdle = db.intOrDefault("minimumIdle", 6)

            connectionTimeout = db.longOrDefault("connectionTimeoutMs", 30_000L)
            idleTimeout = db.longOrDefault("idleTimeoutMs", 600_000L)
            maxLifetime = db.longOrDefault("maxLifetimeMs", 1_800_000L)
            keepaliveTime = db.longOrDefault("keepaliveTimeMs", 0L)
        }

        hc.validate()
        dataSource = HikariDataSource(hc)
    }

    fun getConnection(): Connection {
        check(this::dataSource.isInitialized) { "Database is not initialized" }
        return dataSource.connection
    }

    inline fun <T> withConnection(block: (Connection) -> T): T =
        getConnection().use { conn -> block(conn) }

    inline fun <T> withTransaction(block: (Connection) -> T): T =
        getConnection().use { conn ->
            val prevAuto = conn.autoCommit
            conn.autoCommit = false
            try {
                val result = block(conn)
                conn.commit()
                result
            } catch (t: Throwable) {
                try { conn.rollback() } catch (_: Throwable) {}
                throw t
            } finally {
                try { conn.autoCommit = prevAuto } catch (_: Throwable) {}
            }
        }

    fun close() {
        if (this::dataSource.isInitialized) {
            try { dataSource.close() } catch (_: Throwable) {}
        }
    }
}

private fun env(key: String): String? = System.getenv(key)?.takeIf { it.isNotBlank() }

private fun envRequired(key: String): String =
    env(key) ?: throw IllegalStateException("Required env var '$key' is not set or blank")

private fun ApplicationConfig.intOrDefault(key: String, default: Int): Int =
    propertyOrNull(key)?.getString()?.toIntOrNull() ?: default

private fun ApplicationConfig.longOrDefault(key: String, default: Long): Long =
    propertyOrNull(key)?.getString()?.toLongOrNull() ?: default
