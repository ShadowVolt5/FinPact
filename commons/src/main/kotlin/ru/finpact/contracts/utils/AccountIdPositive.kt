package ru.finpact.contracts.utils

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Precondition

class AccountIdPositive : Precondition {
    override fun verify(ctx: ContractContext) {
        val accountId = ctx.args.getOrNull(1) as? Long
            ?: throw ContractViolation("accountId must be provided")

        if (accountId <= 0L) {
            throw ContractViolation("accountId must be positive")
        }
    }
}

