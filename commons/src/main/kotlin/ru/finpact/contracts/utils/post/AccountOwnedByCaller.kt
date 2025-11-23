package ru.finpact.contracts.utils.post

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Postcondition
import kotlin.reflect.full.memberProperties

class AccountOwnedByCaller : Postcondition {

    override fun verify(ctx: ContractContext) {
        val ownerId = ctx.args.getOrNull(1) as? Long
            ?: throw ContractViolation("ownerId must be provided")

        val result = ctx.result
            ?: throw ContractViolation("result must not be null")

        val kClass = result::class

        val ownerIdProp = kClass.memberProperties
            .firstOrNull { it.name == "ownerId" }
            ?: throw ContractViolation("result.ownerId property is missing")

        val resultOwnerId = ownerIdProp.getter.call(result) as? Long
            ?: throw ContractViolation("result.ownerId must be Long")

        if (resultOwnerId != ownerId) {
            throw ContractViolation("account not found")
        }
    }
}

