package ru.finpact.infra.repository.impl

import ru.finpact.infra.db.Database
import ru.finpact.infra.repository.LimitsRepository
import ru.finpact.model.LimitProfile
import java.math.BigDecimal
import java.sql.ResultSet
import java.time.LocalDate

class LimitsRepositoryImpl : LimitsRepository {

    override fun findProfile(ownerId: Long): LimitProfile? =
        Database.withConnection { conn ->
            conn.prepareStatement(
                """
                SELECT owner_id, per_txn, daily, monthly, currencies
                FROM limits_profiles
                WHERE owner_id = ?
                LIMIT 1
                """.trimIndent()
            ).use { ps ->
                ps.setLong(1, ownerId)
                ps.executeQuery().use { rs ->
                    if (rs.next()) mapProfile(rs) else null
                }
            }
        }

    override fun getDailyUsed(ownerId: Long, day: LocalDate): BigDecimal? =
        Database.withConnection { conn ->
            conn.prepareStatement(
                """
                SELECT used
                FROM limits_usage_daily
                WHERE owner_id = ? AND day = ?
                LIMIT 1
                """.trimIndent()
            ).use { ps ->
                ps.setLong(1, ownerId)
                ps.setObject(2, day)
                ps.executeQuery().use { rs ->
                    if (rs.next()) rs.getBigDecimal("used") else null
                }
            }
        }

    override fun getMonthlyUsed(ownerId: Long, monthStart: LocalDate): BigDecimal? =
        Database.withConnection { conn ->
            conn.prepareStatement(
                """
                SELECT used
                FROM limits_usage_monthly
                WHERE owner_id = ? AND month_start = ?
                LIMIT 1
                """.trimIndent()
            ).use { ps ->
                ps.setLong(1, ownerId)
                ps.setObject(2, monthStart)
                ps.executeQuery().use { rs ->
                    if (rs.next()) rs.getBigDecimal("used") else null
                }
            }
        }

    private fun mapProfile(rs: ResultSet): LimitProfile {
        val arr = rs.getArray("currencies")
        val currencies = when (val raw = arr?.array) {
            is Array<*> -> raw.mapNotNull { it?.toString() }
            else -> emptyList()
        }

        return LimitProfile(
            ownerId = rs.getLong("owner_id"),
            perTxn = rs.getBigDecimal("per_txn"),
            daily = rs.getBigDecimal("daily"),
            monthly = rs.getBigDecimal("monthly"),
            currencies = currencies,
        )
    }
}
