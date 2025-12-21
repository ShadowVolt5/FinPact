package ru.finpact.contracts.utils.pre

import ru.finpact.contracts.core.*

class AccountIdPositive : Precondition {
    override fun verify(ctx: ContractContext) {
        val accountId = ctx.arg<Long>("accountId")
        if (accountId <= 0L) throw ContractViolation.badRequest("accountId must be positive")
    }
}
