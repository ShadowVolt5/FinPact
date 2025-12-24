package ru.finpact.contracts.utils.pre

import ru.finpact.contracts.core.*

class CallerIdPositive : Precondition {
    override fun verify(ctx: ContractContext) {
        val callerId = ctx.arg<Long>("callerId")
        if (callerId <= 0L) throw ContractViolation.badRequest("callerId must be positive")
    }
}
