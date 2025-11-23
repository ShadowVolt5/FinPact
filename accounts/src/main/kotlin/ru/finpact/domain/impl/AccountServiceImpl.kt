package ru.finpact.domain.impl

import ru.finpact.contracts.core.ContractViolation
import ru.finpact.domain.AccountService
import ru.finpact.dto.common.AccountResponse
import ru.finpact.dto.created.OpenAccountRequest
import ru.finpact.dto.deposits.DepositRequest
import ru.finpact.infra.repository.AccountRepository
import java.math.BigDecimal

class AccountServiceImpl(
    private val accountRepository: AccountRepository
) : AccountService {

    override fun openAccount(ownerId: Long, request: OpenAccountRequest): AccountResponse {
        val currency = request.currency.trim().uppercase()
        val alias = request.alias?.trim()?.ifBlank { null }

        val account = accountRepository.createAccount(
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
        val account = accountRepository.findById(accountId)
            ?: throw ContractViolation("account not found with accountId: $accountId")

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

    override fun deposit(ownerId: Long, accountId: Long, request: DepositRequest): AccountResponse {
        val account = accountRepository.findById(accountId)
            ?: throw ContractViolation("account not found with accountId: $accountId")

        if (account.ownerId != ownerId) {
            throw ContractViolation("account not found with ownerId: $ownerId")
        }

        if (!account.isActive) {
            throw ContractViolation("account is not active with accountId: $accountId")
        }

        val amount = BigDecimal(request.amount.trim())
        val updated = accountRepository.deposit(accountId = accountId, amount = amount)

        return AccountResponse(
            id = updated.id,
            ownerId = updated.ownerId,
            currency = updated.currency,
            alias = updated.alias,
            balance = updated.balance.toPlainString(),
            isActive = updated.isActive,
            createdAt = updated.createdAt.toString(),
        )
    }
}
