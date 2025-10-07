package ru.finpact.contracts.core

/**
 * Нарушение контракта (пред/пост/инвариант).
 * В приложениях логируйте и маппите в доменные ошибки/HTTP-коды на своём уровне.
 */
class ContractViolation(message: String) : IllegalStateException(message)
