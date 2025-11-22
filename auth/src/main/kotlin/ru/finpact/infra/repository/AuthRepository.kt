package ru.finpact.infra.repository

import ru.finpact.model.User

interface AuthRepository {

    fun findUserIdByEmail(email: String): Long?

    fun insertUser(
        email: String,
        firstName: String,
        middleName: String?,
        lastName: String,
        passwordHash: String
    ): Long

    fun findUserByEmail(email: String): User?

    fun findUserById(id: Long): User?
}
