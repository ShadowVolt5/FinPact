package ru.finpact.contracts.utils.post

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Postcondition
import kotlin.reflect.full.memberProperties

class PaymentDetailsMatchesRequest : Postcondition {

    override fun verify(ctx: ContractContext) {
        val paymentId = ctx.args.getOrNull(1) as? Long
            ?: throw ContractViolation("paymentId must be provided")

        val res = ctx.result ?: throw ContractViolation("result must not be null")
        val k = res::class

        val id = (k.memberProperties.firstOrNull { it.name == "id" }?.getter?.call(res) as? Long)
            ?: throw ContractViolation("result.id must be provided")

        if (id != paymentId) {
            throw ContractViolation("result.id must match paymentId")
        }
    }
}
