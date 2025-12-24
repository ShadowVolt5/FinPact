package ru.finpact.infra.repository.impl

import ru.finpact.infra.repository.LimitsRepository
import java.math.BigDecimal
import java.sql.Connection

class LimitsRepositoryImpl : LimitsRepository {

    override fun lockProfileForUpdate(conn: Connection, ownerId: Long): LimitsRepository.LimitsProfileRow? =
        conn.prepareStatement(
            """
            SELECT owner_id, base_currency, per_txn, daily, monthly, currencies, kyc_verified, sanctions_clear
            FROM limits.limits_profiles
            WHERE owner_id = ?
            FOR UPDATE
            """.trimIndent()
        ).use { ps ->
            ps.setLong(1, ownerId)
            ps.executeQuery().use { rs ->
                if (!rs.next()) return null

                val arr = rs.getArray("currencies")?.array as? Array<*>
                val currencies = (arr?.mapNotNull { it?.toString() } ?: emptyList())
                    .map { it.trim().uppercase() }
                    .toSet()

                LimitsRepository.LimitsProfileRow(
                    ownerId = rs.getLong("owner_id"),
                    baseCurrency = rs.getString("base_currency").trim().uppercase(),
                    perTxn = rs.getBigDecimal("per_txn"),
                    daily = rs.getBigDecimal("daily"),
                    monthly = rs.getBigDecimal("monthly"),
                    currencies = currencies,
                    kycVerified = rs.getBoolean("kyc_verified"),
                    sanctionsClear = rs.getBoolean("sanctions_clear"),
                )
            }
        }

    override fun getDailyUsed(conn: Connection, ownerId: Long): BigDecimal =
        conn.prepareStatement(
            """
            SELECT used
            FROM limits.limits_usage_daily
            WHERE owner_id = ? AND day = CURRENT_DATE
            LIMIT 1
            """.trimIndent()
        ).use { ps ->
            ps.setLong(1, ownerId)
            ps.executeQuery().use { rs ->
                if (rs.next()) rs.getBigDecimal("used") else BigDecimal.ZERO
            }
        }

    override fun getMonthlyUsed(conn: Connection, ownerId: Long): BigDecimal =
        conn.prepareStatement(
            """
            SELECT used
            FROM limits.limits_usage_monthly
            WHERE owner_id = ? AND month_start = date_trunc('month', CURRENT_DATE)::date
            LIMIT 1
            """.trimIndent()
        ).use { ps ->
            ps.setLong(1, ownerId)
            ps.executeQuery().use { rs ->
                if (rs.next()) rs.getBigDecimal("used") else BigDecimal.ZERO
            }
        }

    override fun addUsage(conn: Connection, ownerId: Long, amountRub: BigDecimal) {
        conn.prepareStatement(
            """
            INSERT INTO limits.limits_usage_daily(owner_id, day, used)
            VALUES (?, CURRENT_DATE, ?)
            ON CONFLICT (owner_id, day) DO UPDATE
            SET used = limits.limits_usage_daily.used + EXCLUDED.used
            """.trimIndent()
        ).use { ps ->
            ps.setLong(1, ownerId)
            ps.setBigDecimal(2, amountRub)
            ps.executeUpdate()
        }

        conn.prepareStatement(
            """
            INSERT INTO limits.limits_usage_monthly(owner_id, month_start, used)
            VALUES (?, date_trunc('month', CURRENT_DATE)::date, ?)
            ON CONFLICT (owner_id, month_start) DO UPDATE
            SET used = limits.limits_usage_monthly.used + EXCLUDED.used
            """.trimIndent()
        ).use { ps ->
            ps.setLong(1, ownerId)
            ps.setBigDecimal(2, amountRub)
            ps.executeUpdate()
        }
    }
}
