package ru.finpact.contracts.core

/**
 * Категория нарушения контракта — чтобы верхний уровень (HTTP) мог вернуть корректный статус.
 */
enum class ContractViolationKind {
    BAD_REQUEST,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    CONFLICT,
    INTERNAL,
}

/**
 * Нарушение контракта (пред/пост/инвариант).
 *
 * kind:
 *  - BAD_REQUEST: ошибка входных данных (400)
 *  - UNAUTHORIZED: проблемы аутентификации (401)
 *  - FORBIDDEN: недостаточно прав (403)
 *  - NOT_FOUND: сущность не найдена (404)
 *  - CONFLICT: конфликт (409)
 *  - INTERNAL: ошибка проверки контракта/инвариантов по причине бага/инфры (500)
 */
class ContractViolation(
    message: String,
    val kind: ContractViolationKind = ContractViolationKind.BAD_REQUEST,
    cause: Throwable? = null
) : IllegalStateException(message, cause) {

    companion object {
        fun badRequest(message: String): ContractViolation =
            ContractViolation(message, ContractViolationKind.BAD_REQUEST)

        fun unauthorized(message: String): ContractViolation =
            ContractViolation(message, ContractViolationKind.UNAUTHORIZED)

        fun forbidden(message: String): ContractViolation =
            ContractViolation(message, ContractViolationKind.FORBIDDEN)

        fun notFound(message: String): ContractViolation =
            ContractViolation(message, ContractViolationKind.NOT_FOUND)

        fun conflict(message: String): ContractViolation =
            ContractViolation(message, ContractViolationKind.CONFLICT)

        fun internal(message: String, cause: Throwable? = null): ContractViolation =
            ContractViolation(message, ContractViolationKind.INTERNAL, cause)
    }
}
