package ru.finpact.contracts.utils.pre

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Precondition

class PaymentIdPositive : Precondition {
    override fun verify(ctx: ContractContext) {
        val paymentId = ctx.args.getOrNull(1) as? Long
            ?: throw ContractViolation("paymentId must be provided")

        if (paymentId <= 0L) {
            throw ContractViolation("paymentId must be positive")
        }
    }
}
