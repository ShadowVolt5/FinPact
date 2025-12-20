package ru.finpact.infra.repository

interface UsersRepository {
    fun existsById(id: Long): Boolean
}
