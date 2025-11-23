package ru.finpact.infra.repository

import ru.finpact.model.Account
import java.math.BigDecimal

interface AccountRepository {

    fun createAccount(
        ownerId: Long,
        currency: String,
        alias: String?,
        initialBalance: BigDecimal = BigDecimal.ZERO,
    ): Account

    fun findById(id: Long): Account?

    fun deposit(accountId: Long, amount: BigDecimal): Account
}
