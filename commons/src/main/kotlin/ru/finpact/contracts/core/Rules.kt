package ru.finpact.contracts.core

fun interface Precondition { fun verify(ctx: ContractContext) }
fun interface Postcondition { fun verify(ctx: ContractContext) }
fun interface InvariantRule { fun verify(ctx: ContractContext) }
