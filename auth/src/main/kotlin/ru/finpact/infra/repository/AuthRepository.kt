package ru.finpact.infra.repository

interface AuthRepository {
    fun findUserIdByEmail(email: String): Long?
    fun insertUser(
        email: String,
        firstName: String,
        middleName: String?,
        lastName: String,
        passwordHash: String
    ): Long
}
