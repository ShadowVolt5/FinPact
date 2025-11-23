package ru.finpact.infra.repository.impl

import ru.finpact.infra.db.Database
import ru.finpact.infra.repository.AccountsRepository
import ru.finpact.model.Account
import java.math.BigDecimal
import java.sql.ResultSet
import java.sql.Types

class AccountsRepositoryImpl : AccountsRepository {

    override fun createAccount(
        ownerId: Long,
        currency: String,
        alias: String?,
        initialBalance: BigDecimal,
    ): Account =
        Database.withTransaction { conn ->
            conn.prepareStatement(
                """
                INSERT INTO accounts(owner_id, currency, alias, balance, is_active)
                VALUES (?, ?, ?, ?, TRUE)
                RETURNING id, owner_id, currency, alias, balance, is_active
                """.trimIndent()
            ).use { ps ->
                ps.setLong(1, ownerId)
                ps.setString(2, currency)
                if (alias == null) {
                    ps.setNull(3, Types.VARCHAR)
                } else {
                    ps.setString(3, alias)
                }
                ps.setBigDecimal(4, initialBalance)

                ps.executeQuery().use { rs ->
                    require(rs.next()) { "INSERT accounts did not return row" }
                    mapAccount(rs)
                }
            }
        }

    private fun mapAccount(rs: ResultSet) = Account(
        id = rs.getLong("id"),
        ownerId = rs.getLong("owner_id"),
        currency = rs.getString("currency"),
        alias = rs.getString("alias"),
        balance = rs.getBigDecimal("balance"),
        isActive = rs.getBoolean("is_active"),
    )
}
