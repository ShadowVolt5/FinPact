package ru.finpact.contracts.annotations

import kotlin.reflect.KClass
import ru.finpact.contracts.core.Postcondition

/**
 * Постусловия метода: гарантии ПОСЛЕ выполнения.
 * Проверяются на возвращаемом значении/состоянии; нарушение — ContractViolation.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Post(vararg val rules: KClass<out Postcondition>)
