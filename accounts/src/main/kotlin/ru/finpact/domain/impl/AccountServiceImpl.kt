package ru.finpact.domain.impl

import ru.finpact.domain.AccountService
import ru.finpact.dto.create.OpenAccountRequest
import ru.finpact.dto.create.OpenAccountResponse
import ru.finpact.infra.repository.AccountsRepository
import java.math.BigDecimal

class AccountServiceImpl(
    private val accountsRepository: AccountsRepository
) : AccountService {

    override fun openAccount(ownerId: Long, request: OpenAccountRequest): OpenAccountResponse {
        val currency = request.currency.trim().uppercase()
        val alias = request.alias?.trim()?.ifBlank { null }

        val account = accountsRepository.createAccount(
            ownerId = ownerId,
            currency = currency,
            alias = alias,
            initialBalance = BigDecimal.ZERO,
        )

        return OpenAccountResponse(
            id = account.id,
            ownerId = account.ownerId,
            currency = account.currency,
            alias = account.alias,
            balance = account.balance.toPlainString(),
            isActive = account.isActive,
        )
    }
}
