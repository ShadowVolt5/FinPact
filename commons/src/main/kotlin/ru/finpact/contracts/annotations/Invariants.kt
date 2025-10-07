package ru.finpact.contracts.annotations

import kotlin.reflect.KClass
import ru.finpact.contracts.core.InvariantRule

/**
 * Инварианты класса: свойства, которые должны оставаться истинными ДО и ПОСЛЕ
 * каждого публичного вызова (в контексте нашего proxy — любого метода интерфейса).
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Invariants(vararg val rules: KClass<out InvariantRule>)
