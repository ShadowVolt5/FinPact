package ru.finpact.infra.repository.impl

import ru.finpact.contracts.core.ContractViolation
import ru.finpact.infra.db.Database
import ru.finpact.infra.repository.PaymentRepository
import ru.finpact.model.Transfer
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.Connection
import java.sql.ResultSet

class PaymentRepositoryImpl : PaymentRepository {

    override fun createTransfer(
        initiatedBy: Long,
        fromAccountId: Long,
        toAccountId: Long,
        amount: BigDecimal,
        description: String?,
    ): Transfer = Database.withTransaction { conn ->
        val normalizedAmount = amount.setScale(4, RoundingMode.UNNECESSARY)

        val (firstId, secondId) =
            if (fromAccountId < toAccountId) fromAccountId to toAccountId else toAccountId to fromAccountId

        val a1 = lockAccount(conn, firstId)
            ?: throw ContractViolation("account not found")
        val a2 = lockAccount(conn, secondId)
            ?: throw ContractViolation("account not found")

        val from = if (a1.id == fromAccountId) a1 else a2
        val to = if (a1.id == toAccountId) a1 else a2

        if (!from.isActive) throw ContractViolation("account is not active")
        if (!to.isActive) throw ContractViolation("account is not active")

        if (from.ownerId != initiatedBy) throw ContractViolation("account not found")

        val fromCurrency = from.currency.trim().uppercase()
        val toCurrency = to.currency.trim().uppercase()
        if (fromCurrency != toCurrency) throw ContractViolation("currency mismatch")

        if (from.balance < normalizedAmount) throw ContractViolation("insufficient funds")

        withdraw(conn, fromAccountId, normalizedAmount)
        deposit(conn, toAccountId, normalizedAmount)

        insertTransfer(
            conn = conn,
            initiatedBy = initiatedBy,
            fromAccountId = fromAccountId,
            toAccountId = toAccountId,
            amount = normalizedAmount,
            currency = fromCurrency,
            description = description?.trim()?.ifBlank { null },
        )
    }

    private data class AccountRow(
        val id: Long,
        val ownerId: Long,
        val currency: String,
        val balance: BigDecimal,
        val isActive: Boolean,
    )

    private fun lockAccount(conn: Connection, id: Long): AccountRow? =
        conn.prepareStatement(
            """
            SELECT id, owner_id, currency, balance, is_active
            FROM accounts
            WHERE id = ?
            FOR UPDATE
            """.trimIndent()
        ).use { ps ->
            ps.setLong(1, id)
            ps.executeQuery().use { rs ->
                if (!rs.next()) return null
                mapAccount(rs)
            }
        }

    private fun withdraw(conn: Connection, accountId: Long, amount: BigDecimal) {
        conn.prepareStatement(
            """
            UPDATE accounts
            SET balance = balance - ?
            WHERE id = ?
            """.trimIndent()
        ).use { ps ->
            ps.setBigDecimal(1, amount)
            ps.setLong(2, accountId)
            val updated = ps.executeUpdate()
            if (updated != 1) throw ContractViolation("account not found")
        }
    }

    private fun deposit(conn: Connection, accountId: Long, amount: BigDecimal) {
        conn.prepareStatement(
            """
            UPDATE accounts
            SET balance = balance + ?
            WHERE id = ?
            """.trimIndent()
        ).use { ps ->
            ps.setBigDecimal(1, amount)
            ps.setLong(2, accountId)
            val updated = ps.executeUpdate()
            if (updated != 1) throw ContractViolation("account not found")
        }
    }

    private fun insertTransfer(
        conn: Connection,
        initiatedBy: Long,
        fromAccountId: Long,
        toAccountId: Long,
        amount: BigDecimal,
        currency: String,
        description: String?,
    ): Transfer =
        conn.prepareStatement(
            """
            INSERT INTO transfers(
                from_account_id,
                to_account_id,
                amount,
                currency,
                description,
                initiated_by
            )
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id, created_at
            """.trimIndent()
        ).use { ps ->
            ps.setLong(1, fromAccountId)
            ps.setLong(2, toAccountId)
            ps.setBigDecimal(3, amount)
            ps.setString(4, currency)
            ps.setString(5, description)
            ps.setLong(6, initiatedBy)

            ps.executeQuery().use { rs ->
                require(rs.next()) { "INSERT transfers did not return row" }
                val id = rs.getLong("id")
                val createdAt = rs.getTimestamp("created_at").toInstant()

                Transfer(
                    id = id,
                    fromAccountId = fromAccountId,
                    toAccountId = toAccountId,
                    amount = amount,
                    currency = currency,
                    description = description,
                    initiatedBy = initiatedBy,
                    createdAt = createdAt,
                )
            }
        }

    private fun mapAccount(rs: ResultSet) = AccountRow(
        id = rs.getLong("id"),
        ownerId = rs.getLong("owner_id"),
        currency = rs.getString("currency"),
        balance = rs.getBigDecimal("balance"),
        isActive = rs.getBoolean("is_active"),
    )
}
