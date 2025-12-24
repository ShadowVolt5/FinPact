package ru.finpact.infra.repository.impl

import ru.finpact.infra.db.Database
import ru.finpact.infra.repository.UsersRepository

class UsersRepositoryImpl : UsersRepository {

    override fun existsById(id: Long): Boolean =
        Database.withConnection { conn ->
            conn.prepareStatement(
                """
                SELECT 1
                FROM auth.users
                WHERE id = ?
                LIMIT 1
                """.trimIndent()
            ).use { ps ->
                ps.setLong(1, id)
                ps.executeQuery().use { rs -> rs.next() }
            }
        }
}
