package ru.finpact.domain.impl

import ru.finpact.contracts.core.ContractViolation
import ru.finpact.domain.AccountService
import ru.finpact.dto.common.AccountResponse
import ru.finpact.dto.create.OpenAccountRequest
import ru.finpact.infra.repository.AccountsRepository
import java.math.BigDecimal

class AccountServiceImpl(
    private val accountsRepository: AccountsRepository
) : AccountService {

    override fun openAccount(ownerId: Long, request: OpenAccountRequest): AccountResponse {
        val currency = request.currency.trim().uppercase()
        val alias = request.alias?.trim()?.ifBlank { null }

        val account = accountsRepository.createAccount(
            ownerId = ownerId,
            currency = currency,
            alias = alias,
            initialBalance = BigDecimal.ZERO,
        )

        return AccountResponse(
            id = account.id,
            ownerId = account.ownerId,
            currency = account.currency,
            alias = account.alias,
            balance = account.balance.toPlainString(),
            isActive = account.isActive,
            createdAt = account.createdAt.toString()
        )
    }

    override fun getAccount(accountId: Long, ownerId: Long): AccountResponse {
        val account = accountsRepository.findById(accountId)
            ?: throw ContractViolation("account not found")

        return AccountResponse(
            id = account.id,
            ownerId = account.ownerId,
            currency = account.currency,
            alias = account.alias,
            balance = account.balance.toPlainString(),
            isActive = account.isActive,
            createdAt = account.createdAt.toString(),
        )
    }
}
