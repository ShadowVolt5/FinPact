package ru.finpact.domain

import ru.finpact.contracts.annotations.Post
import ru.finpact.contracts.annotations.Pre
import ru.finpact.contracts.utils.post.AccountClosedWithZeroBalance
import ru.finpact.contracts.utils.post.AccountOwnedByCaller
import ru.finpact.contracts.utils.post.ResultNotNull
import ru.finpact.contracts.utils.pre.AccountIdPositive
import ru.finpact.contracts.utils.pre.DepositRequestValid
import ru.finpact.contracts.utils.pre.OpenAccountRequestValid
import ru.finpact.contracts.utils.pre.OwnerIdPositive
import ru.finpact.dto.common.AccountResponse
import ru.finpact.dto.created.OpenAccountRequest
import ru.finpact.dto.deposits.DepositRequest

interface AccountService {

    @Pre(
        OwnerIdPositive::class,
        OpenAccountRequestValid::class,
    )
    @Post(ResultNotNull::class)
    fun openAccount(ownerId: Long, request: OpenAccountRequest): AccountResponse

    @Pre(
        AccountIdPositive::class,
        OwnerIdPositive::class,
    )
    @Post(
        ResultNotNull::class,
        AccountOwnedByCaller::class,
    )
    fun getAccount(accountId: Long, ownerId: Long): AccountResponse

    @Pre(
        OwnerIdPositive::class,
        AccountIdPositive::class,
        DepositRequestValid::class,
    )
    @Post(ResultNotNull::class)
    fun deposit(ownerId: Long, accountId: Long, request: DepositRequest): AccountResponse

    @Pre(
        AccountIdPositive::class,
        OwnerIdPositive::class,
    )
    @Post(
        ResultNotNull::class,
        AccountOwnedByCaller::class,
        AccountClosedWithZeroBalance::class,
    )
    fun closeAccount(accountId: Long, ownerId: Long): AccountResponse
}
