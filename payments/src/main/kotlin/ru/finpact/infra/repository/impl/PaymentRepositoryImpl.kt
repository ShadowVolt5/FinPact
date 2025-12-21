package ru.finpact.infra.repository.impl

import ru.finpact.contracts.core.ContractViolation
import ru.finpact.infra.db.Database
import ru.finpact.infra.repository.PaymentRepository
import ru.finpact.model.*
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
    ): Transfer {
        val outcome: CreateTransferOutcome = Database.withTransaction { conn ->
            val normalizedAmount = amount.setScale(4, RoundingMode.UNNECESSARY)
            val normalizedDescription = description?.trim()?.ifBlank { null }

            val (firstId, secondId) =
                if (fromAccountId < toAccountId) fromAccountId to toAccountId else toAccountId to fromAccountId

            val a1 = lockAccount(conn, firstId) ?: return@withTransaction failNoInsert("account not found")
            val a2 = lockAccount(conn, secondId) ?: return@withTransaction failNoInsert("account not found")

            val from = if (a1.id == fromAccountId) a1 else a2
            val to = if (a1.id == toAccountId) a1 else a2

            if (from.ownerId != initiatedBy) {
                return@withTransaction failNoInsert("account not found")
            }

            val currency = from.currency.trim().uppercase()

            val reason: String? = when {
                !from.isActive -> "account is not active"
                !to.isActive -> "account not found"
                from.currency.trim().uppercase() != to.currency.trim().uppercase() -> "account not found"
                from.balance < normalizedAmount -> "insufficient funds"
                else -> null
            }

            if (reason != null) {
                val failed = insertTransfer(
                    conn = conn,
                    initiatedBy = initiatedBy,
                    fromAccountId = fromAccountId,
                    toAccountId = toAccountId,
                    amount = normalizedAmount,
                    currency = currency,
                    status = PaymentStatus.FAILED,
                    description = normalizedDescription,
                )
                return@withTransaction CreateTransferOutcome.Failure(reason, failed.id)
            }

            withdraw(conn, fromAccountId, normalizedAmount)
            deposit(conn, toAccountId, normalizedAmount)

            val completed = insertTransfer(
                conn = conn,
                initiatedBy = initiatedBy,
                fromAccountId = fromAccountId,
                toAccountId = toAccountId,
                amount = normalizedAmount,
                currency = currency,
                status = PaymentStatus.COMPLETED,
                description = normalizedDescription,
            )

            CreateTransferOutcome.Success(completed)
        }

        return when (outcome) {
            is CreateTransferOutcome.Success -> outcome.transfer
            is CreateTransferOutcome.Failure -> throw ContractViolation(outcome.reason)
            is CreateTransferOutcome.FailureNoInsert -> throw ContractViolation(outcome.reason)
        }
    }

    override fun findPaymentDetails(initiatedBy: Long, paymentId: Long): PaymentDetails? =
        Database.withConnection { conn ->
            conn.prepareStatement(
                """
                SELECT
                    t.id,
                    t.status,
                    t.from_account_id,
                    t.to_account_id,
                    t.amount,
                    t.currency,
                    t.description,
                    t.created_at,

                    af.id AS from_id,
                    af.currency AS from_currency,
                    af.balance AS from_balance,
                    af.is_active AS from_active,

                    at.id AS to_id,
                    at.currency AS to_currency

                FROM transfers t
                JOIN accounts af ON af.id = t.from_account_id
                JOIN accounts at ON at.id = t.to_account_id
                WHERE t.id = ? AND t.initiated_by = ?
                LIMIT 1
                """.trimIndent()
            ).use { ps ->
                ps.setLong(1, paymentId)
                ps.setLong(2, initiatedBy)
                ps.executeQuery().use { rs ->
                    if (!rs.next()) return@withConnection null
                    mapPaymentDetails(rs)
                }
            }
        }

    override fun searchTransfers(
        initiatedBy: Long,
        filter: TransferSearchFilter,
        limit: Int,
        offset: Long,
    ): TransferSearchPage =
        Database.withConnection { conn ->
            val sql = buildString {
                append(
                    """
                    SELECT id, from_account_id, to_account_id, amount, currency, status, description, initiated_by, created_at
                    FROM transfers
                    WHERE initiated_by = ?
                    """.trimIndent()
                )

                if (filter.status != null) append(" AND status = ?")
                if (filter.fromAccountId != null) append(" AND from_account_id = ?")
                if (filter.toAccountId != null) append(" AND to_account_id = ?")
                if (filter.currency != null) append(" AND currency = ?")
                if (filter.createdFrom != null) append(" AND created_at >= ?")
                if (filter.createdTo != null) append(" AND created_at <= ?")

                append(" ORDER BY created_at DESC, id DESC")
                append(" LIMIT ? OFFSET ?")
            }

            val fetchLimit = limit + 1

            conn.prepareStatement(sql).use { ps ->
                var i = 1
                ps.setLong(i++, initiatedBy)

                filter.status?.let { ps.setString(i++, it.name) }
                filter.fromAccountId?.let { ps.setLong(i++, it) }
                filter.toAccountId?.let { ps.setLong(i++, it) }
                filter.currency?.let { ps.setString(i++, it.trim().uppercase()) }
                filter.createdFrom?.let { ps.setTimestamp(i++, java.sql.Timestamp.from(it)) }
                filter.createdTo?.let { ps.setTimestamp(i++, java.sql.Timestamp.from(it)) }

                ps.setInt(i++, fetchLimit)
                ps.setLong(i++, offset)

                val rows = mutableListOf<Transfer>()
                ps.executeQuery().use { rs ->
                    while (rs.next()) rows += mapTransferRow(rs)
                }

                val hasMore = rows.size > limit
                val items = if (hasMore) rows.take(limit) else rows

                TransferSearchPage(items = items, hasMore = hasMore)
            }
        }

    private sealed interface CreateTransferOutcome {
        data class Success(val transfer: Transfer) : CreateTransferOutcome
        data class Failure(val reason: String, val paymentId: Long) : CreateTransferOutcome
        data class FailureNoInsert(val reason: String) : CreateTransferOutcome
    }

    private fun failNoInsert(reason: String): CreateTransferOutcome =
        CreateTransferOutcome.FailureNoInsert(reason)

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
                AccountRow(
                    id = rs.getLong("id"),
                    ownerId = rs.getLong("owner_id"),
                    currency = rs.getString("currency"),
                    balance = rs.getBigDecimal("balance"),
                    isActive = rs.getBoolean("is_active"),
                )
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
            if (ps.executeUpdate() != 1) throw ContractViolation("account not found")
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
            if (ps.executeUpdate() != 1) throw ContractViolation("account not found")
        }
    }

    private fun insertTransfer(
        conn: Connection,
        initiatedBy: Long,
        fromAccountId: Long,
        toAccountId: Long,
        amount: BigDecimal,
        currency: String,
        status: PaymentStatus,
        description: String?,
    ): Transfer =
        conn.prepareStatement(
            """
            INSERT INTO transfers(
                from_account_id,
                to_account_id,
                amount,
                currency,
                status,
                description,
                initiated_by
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id, created_at
            """.trimIndent()
        ).use { ps ->
            ps.setLong(1, fromAccountId)
            ps.setLong(2, toAccountId)
            ps.setBigDecimal(3, amount)
            ps.setString(4, currency)
            ps.setString(5, status.name)
            ps.setString(6, description)
            ps.setLong(7, initiatedBy)

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
                    status = status,
                    description = description,
                    initiatedBy = initiatedBy,
                    createdAt = createdAt,
                )
            }
        }

    private fun mapTransferRow(rs: ResultSet): Transfer {
        val statusRaw = rs.getString("status")
        val status = try {
            PaymentStatus.valueOf(statusRaw)
        } catch (_: Throwable) {
            throw ContractViolation("invalid payment status")
        }

        return Transfer(
            id = rs.getLong("id"),
            fromAccountId = rs.getLong("from_account_id"),
            toAccountId = rs.getLong("to_account_id"),
            amount = rs.getBigDecimal("amount"),
            currency = rs.getString("currency").trim().uppercase(),
            status = status,
            description = rs.getString("description"),
            initiatedBy = rs.getLong("initiated_by"),
            createdAt = rs.getTimestamp("created_at").toInstant(),
        )
    }

    private fun mapPaymentDetails(rs: ResultSet): PaymentDetails {
        val id = rs.getLong("id")

        val statusRaw = rs.getString("status")
        val status = try {
            PaymentStatus.valueOf(statusRaw)
        } catch (_: Throwable) {
            throw ContractViolation("invalid payment status")
        }

        val currency = rs.getString("currency").trim().uppercase()

        val from = OwnerAccountSlice(
            id = rs.getLong("from_id"),
            currency = rs.getString("from_currency").trim().uppercase(),
            balance = rs.getBigDecimal("from_balance"),
            isActive = rs.getBoolean("from_active"),
        )

        val to = CounterpartyAccountRef(
            id = rs.getLong("to_id"),
            currency = rs.getString("to_currency").trim().uppercase(),
        )

        return PaymentDetails(
            id = id,
            status = status,
            from = from,
            to = to,
            amount = rs.getBigDecimal("amount"),
            currency = currency,
            description = rs.getString("description"),
            createdAt = rs.getTimestamp("created_at").toInstant(),
        )
    }
}
