package ru.finpact.contracts.annotations

import kotlin.reflect.KClass
import ru.finpact.contracts.core.Precondition

/**
 * Предусловия метода: то, что должно быть истинно ДО вызова.
 * Если хотя бы одно правило ложно — вызов не должен происходить (бросаем ContractViolation).
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Pre(vararg val rules: KClass<out Precondition>)
