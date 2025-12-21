package ru.finpact.contracts.utils.pre

import ru.finpact.contracts.core.*

class Arg0NotNull : Precondition {
    override fun verify(ctx: ContractContext) {
        if (ctx.args.getOrNull(0) == null) {
            throw ContractViolation.badRequest("request must not be null")
        }
    }
}
