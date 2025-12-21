package ru.finpact.contracts.utils.post

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Postcondition
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

class RefundListMatchesRequest : Postcondition {

    override fun verify(ctx: ContractContext) {
        val res = ctx.result ?: throw ContractViolation("result must not be null")

        val paymentId = (ctx.args.getOrNull(1) as? Long)
            ?: throw ContractViolation("paymentId must be provided")

        val items = readAny(res::class, res, "items")
        if (items !is List<*>) throw ContractViolation("result.items must be a list")

        for ((idx, item) in items.withIndex()) {
            val it = item ?: throw ContractViolation("result.items[$idx] must not be null")
            val original = readLong(it::class, it, "originalPaymentId")
            if (original != paymentId) {
                throw ContractViolation("result.items[$idx].originalPaymentId must match paymentId")
            }
        }
    }

    private fun readAny(k: KClass<*>, obj: Any, name: String): Any =
        k.memberProperties.firstOrNull { it.name == name }?.getter?.call(obj)
            ?: throw ContractViolation("result.$name must be provided")

    private fun readLong(k: KClass<*>, obj: Any, name: String): Long =
        (k.memberProperties.firstOrNull { it.name == name }?.getter?.call(obj) as? Long)
            ?: throw ContractViolation("result.$name must be provided")
}
