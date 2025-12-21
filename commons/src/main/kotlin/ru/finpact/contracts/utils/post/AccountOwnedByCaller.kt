package ru.finpact.contracts.utils.post

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Postcondition
import ru.finpact.contracts.core.arg
import kotlin.reflect.full.memberProperties

class AccountOwnedByCaller : Postcondition {

    override fun verify(ctx: ContractContext) {
        val ownerId = ctx.arg<Long>("ownerId")

        val result = ctx.result ?: throw ContractViolation.internal("result must not be null")
        val k = result::class

        val ownerIdProp = k.memberProperties
            .firstOrNull { it.name == "ownerId" }
            ?: throw ContractViolation.internal("result.ownerId property is missing")

        val resultOwnerId = ownerIdProp.getter.call(result) as? Long
            ?: throw ContractViolation.internal("result.ownerId must be Long")

        if (resultOwnerId != ownerId) {
            throw ContractViolation.notFound("account not found")
        }
    }
}
