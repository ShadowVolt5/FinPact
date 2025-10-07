package ru.finpact.contracts.core

import java.lang.reflect.Method

data class ContractContext(
    val target: Any,
    val method: Method,
    val args: List<Any?>,
    var result: Any? = null
)
