package ru.finpact.contracts.core

import kotlin.reflect.jvm.kotlinFunction

/**
 * Вспомогалки для чтения аргументов в правилах.
 * Если компилируете с сохранением имён параметров (для Kotlin — это доступно через kotlin-reflect),
 * можно получить аргумент по имени; иначе используйте индекс.
 */

@Suppress("UNCHECKED_CAST")
fun <T> ContractContext.arg(index: Int): T =
    (args.getOrNull(index) as? T)
        ?: throw ContractViolation("Arg#$index is missing or has unexpected type")

@Suppress("UNCHECKED_CAST")
fun <T> ContractContext.arg(name: String): T {
    val kfun = method.kotlinFunction
        ?: throw ContractViolation("No kotlin metadata for method ${method.name}")
    val idx = kfun.parameters
        .filter { it.kind == kotlin.reflect.KParameter.Kind.VALUE }
        .indexOfFirst { it.name == name }
    if (idx < 0) throw ContractViolation("Arg '$name' not found in ${method.name}")
    return args[idx] as? T ?: throw ContractViolation("Arg '$name' has unexpected type")
}
