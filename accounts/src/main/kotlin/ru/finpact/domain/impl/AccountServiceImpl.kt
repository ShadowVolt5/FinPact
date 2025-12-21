package ru.finpact.domain.impl

import ru.finpact.contracts.annotations.Invariants
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.utils.invariants.StatelessServiceInvariant
import ru.finpact.domain.AccountService
import ru.finpact.dto.common.AccountResponse
import ru.finpact.dto.created.OpenAccountRequest
import ru.finpact.dto.deposits.DepositRequest
import ru.finpact.infra.repository.AccountRepository
import java.math.BigDecimal

@Invariants(StatelessServiceInvariant::class)
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

        return account.toDto()
    }

    override fun getAccount(accountId: Long, ownerId: Long): AccountResponse {
        val account = accountRepository.findById(accountId)
            ?: throw ContractViolation.notFound("account not found")

        if (account.ownerId != ownerId) {
            throw ContractViolation.notFound("account not found")
        }

        return account.toDto()
    }

    override fun deposit(ownerId: Long, accountId: Long, request: DepositRequest): AccountResponse {
        val account = accountRepository.findById(accountId)
            ?: throw ContractViolation.notFound("account not found")

        if (account.ownerId != ownerId) {
            throw ContractViolation.notFound("account not found")
        }

        if (!account.isActive) {
            throw ContractViolation.conflict("account is not active")
        }

        val amount = try {
            BigDecimal(request.amount.trim())
        } catch (_: Throwable) {
            throw ContractViolation.badRequest("amount must be a valid decimal number")
        }

        val updated = accountRepository.deposit(accountId = accountId, amount = amount)
        return updated.toDto()
    }
}

private fun ru.finpact.model.Account.toDto() = AccountResponse(
    id = id,
    ownerId = ownerId,
    currency = currency,
    alias = alias,
    balance = balance.toPlainString(),
    isActive = isActive,
    createdAt = createdAt.toString(),
)
