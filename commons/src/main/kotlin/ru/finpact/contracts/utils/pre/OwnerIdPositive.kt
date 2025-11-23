package ru.finpact.contracts.utils.pre

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Precondition

class OwnerIdPositive : Precondition {
    override fun verify(ctx: ContractContext) {
        val ownerId = ctx.args.getOrNull(0) as? Long
            ?: throw ContractViolation("ownerId must be provided")

        if (ownerId <= 0L) {
            throw ContractViolation("ownerId must be positive")
        }
    }
}
