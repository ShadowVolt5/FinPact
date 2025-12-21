package ru.finpact.contracts.utils.pre

import ru.finpact.contracts.core.*

class OwnerIdPositive : Precondition {
    override fun verify(ctx: ContractContext) {
        val ownerId = ctx.arg<Long>("ownerId")
        if (ownerId <= 0L) throw ContractViolation.badRequest("ownerId must be positive")
    }
}
