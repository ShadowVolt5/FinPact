package ru.finpact.contracts.ports

/**
 * Порт для проверки существования пользователя (sub из JWT).
 * Реализуется тем сервисом, который знает, где хранится пользователь.
 */
interface SubjectExistencePort {
    fun subjectExists(userId: Long): Boolean
}
