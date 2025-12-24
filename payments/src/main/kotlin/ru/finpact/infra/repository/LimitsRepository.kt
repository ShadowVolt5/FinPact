package ru.finpact.infra.repository

import java.math.BigDecimal
import java.sql.Connection

interface LimitsRepository {

    data class LimitsProfileRow(
        val ownerId: Long,
        val baseCurrency: String,
        val perTxn: BigDecimal,
        val daily: BigDecimal,
        val monthly: BigDecimal,
        val currencies: Set<String>,
        val kycVerified: Boolean,
        val sanctionsClear: Boolean,
    )

    fun lockProfileForUpdate(conn: Connection, ownerId: Long): LimitsProfileRow?

    fun getDailyUsed(conn: Connection, ownerId: Long): BigDecimal
    fun getMonthlyUsed(conn: Connection, ownerId: Long): BigDecimal

    fun addUsage(conn: Connection, ownerId: Long, amountRub: BigDecimal)
}
