package ru.finpact.contracts.utils.pre

import ru.finpact.contracts.core.*

class PaymentIdPositive : Precondition {
    override fun verify(ctx: ContractContext) {
        val paymentId = ctx.arg<Long>("paymentId")
        if (paymentId <= 0L) throw ContractViolation.badRequest("paymentId must be positive")
    }
}
