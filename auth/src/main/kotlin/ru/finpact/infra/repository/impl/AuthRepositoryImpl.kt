package ru.finpact.infra.repository.impl

import ru.finpact.infra.db.Database
import ru.finpact.infra.repository.AuthRepository
import ru.finpact.limits.DefaultLimitsPolicy
import ru.finpact.model.User
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types

class AuthRepositoryImpl : AuthRepository {

    override fun findUserIdByEmail(email: String): Long? =
        Database.withConnection { conn ->
            conn.prepareStatement(
                """
                SELECT id
                FROM users
                WHERE lower(email) = lower(?)
                LIMIT 1
                """.trimIndent()
            ).use { ps ->
                ps.setString(1, email)
                ps.executeQuery().use { rs ->
                    if (rs.next()) rs.getLong(1) else null
                }
            }
        }

    override fun insertUser(
        email: String,
        firstName: String,
        middleName: String?,
        lastName: String,
        passwordHash: String
    ): Long =
        Database.withTransaction { conn ->
            val userId = conn.prepareStatement(
                """
                INSERT INTO users(email, first_name, middle_name, last_name, password)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """.trimIndent()
            ).use { ps ->
                ps.setString(1, email)
                ps.setString(2, firstName)
                if (middleName == null) ps.setNull(3, Types.VARCHAR) else ps.setString(3, middleName)
                ps.setString(4, lastName)
                ps.setString(5, passwordHash)

                ps.executeQuery().use { rs ->
                    require(rs.next()) { "INSERT users did not return id" }
                    rs.getLong(1)
                }
            }

            insertDefaultLimitsProfile(conn, userId)

            userId
        }

    override fun findUserByEmail(email: String): User? =
        Database.withConnection { conn ->
            conn.prepareStatement(
                """
                SELECT id, email, first_name, middle_name, last_name, password
                FROM users
                WHERE lower(email) = lower(?)
                LIMIT 1
                """.trimIndent()
            ).use { ps ->
                ps.setString(1, email)
                ps.executeQuery().use { rs ->
                    if (rs.next()) mapUser(rs) else null
                }
            }
        }

    override fun findUserById(id: Long): User? =
        Database.withConnection { conn ->
            conn.prepareStatement(
                """
                SELECT id, email, first_name, middle_name, last_name, password
                FROM users
                WHERE id = ?
                LIMIT 1
                """.trimIndent()
            ).use { ps ->
                ps.setLong(1, id)
                ps.executeQuery().use { rs ->
                    if (rs.next()) mapUser(rs) else null
                }
            }
        }

    private fun insertDefaultLimitsProfile(conn: Connection, userId: Long) {
        val perTxn = DefaultLimitsPolicy.PER_TXN
        val daily = DefaultLimitsPolicy.DAILY
        val monthly = DefaultLimitsPolicy.MONTHLY
        val currencies = DefaultLimitsPolicy.BASE_CURRENCY

        conn.prepareStatement(
            """
            INSERT INTO limits.limits_profiles(
                owner_id, base_currency, per_txn, daily, monthly, currencies, kyc_verified, sanctions_clear
            )
            VALUES (?, 'RUB', ?, ?, ?, ?, TRUE, TRUE)
            ON CONFLICT (owner_id) DO NOTHING
            """.trimIndent()
        ).use { ps ->
            ps.setLong(1, userId)
            ps.setBigDecimal(2, perTxn)
            ps.setBigDecimal(3, daily)
            ps.setBigDecimal(4, monthly)
            ps.setArray(5, conn.createArrayOf("text", currencies))
            ps.executeUpdate()
        }
    }

    private fun mapUser(rs: ResultSet) = User(
        id = rs.getLong("id"),
        email = rs.getString("email"),
        firstName = rs.getString("first_name"),
        middleName = rs.getString("middle_name"),
        lastName = rs.getString("last_name"),
        password = rs.getString("password")
    )
}
