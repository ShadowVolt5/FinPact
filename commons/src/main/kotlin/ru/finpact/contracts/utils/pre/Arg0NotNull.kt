package ru.finpact.contracts.utils.pre

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Precondition

class Arg0NotNull : Precondition {
    override fun verify(ctx: ContractContext) {
        if (ctx.args.getOrNull(0) == null) {
            throw ContractViolation("request must not be null")
        }
    }
}
