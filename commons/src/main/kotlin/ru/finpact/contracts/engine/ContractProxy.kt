package ru.finpact.contracts.engine

import ru.finpact.contracts.annotations.Invariants
import ru.finpact.contracts.annotations.Post
import ru.finpact.contracts.annotations.Pre
import ru.finpact.contracts.core.*
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

object ContractProxy {

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> wrap(target: T): T {
        val intf = T::class.java
        require(intf.isInterface) {
            "ContractProxy supports interfaces. Expose an interface for ${target::class.java.name}"
        }
        val handler = Handler(target)
        return Proxy.newProxyInstance(intf.classLoader, arrayOf(intf), handler) as T
    }

    @PublishedApi
    internal class Handler(private val target: Any) : InvocationHandler {

        private val classInvariants: List<InvariantRule> by lazy {
            instantiateInvariants(target::class.findAnnotation<Invariants>())
        }
        private val preCache = ConcurrentHashMap<Method, List<Precondition>>()
        private val postCache = ConcurrentHashMap<Method, List<Postcondition>>()

        override fun invoke(proxy: Any, method: Method, args: Array<out Any?>?): Any? {
            if (method.declaringClass == Any::class.java) {
                return method.invoke(target, *(args ?: emptyArray()))
            }

            val arrayArgs = args ?: emptyArray()
            val before = ContractContext(target, method, arrayArgs.toList())

            runRules(classInvariants, before, "invariant(before)")
            runRules(preCache.getOrPut(method) { instantiatePre(method) }, before, "pre")

            val result = try {
                method.invoke(target, *arrayArgs)
            } catch (ite: InvocationTargetException) {
                val cause = ite.targetException ?: ite.cause ?: ite
                when (cause) {
                    is ContractViolation -> throw cause
                    is RuntimeException -> throw cause
                    is Error -> throw cause
                    else -> throw RuntimeException(cause)
                }
            }

            val after = before.copy(result = result)
            runRules(postCache.getOrPut(method) { instantiatePost(method) }, after, "post")
            runRules(classInvariants, after, "invariant(after)")

            return result
        }

        private fun runRules(rules: List<Any>, ctx: ContractContext, phase: String) {
            rules.forEach { rule ->
                try {
                    when (rule) {
                        is Precondition -> rule.verify(ctx)
                        is Postcondition -> rule.verify(ctx)
                        is InvariantRule -> rule.verify(ctx)
                        else -> error("Unknown rule type: $rule")
                    }
                } catch (e: ContractViolation) {
                    throw e
                } catch (t: Throwable) {
                    throw ContractViolation.internal(
                        "$phase failed in ${ctx.method.declaringClass.simpleName}.${ctx.method.name}",
                        t
                    )
                }
            }
        }

        private fun instantiatePre(m: Method): List<Precondition> =
            m.getAnnotation(Pre::class.java)?.rules
                ?.map { newInstance(it) } ?: emptyList()

        private fun instantiatePost(m: Method): List<Postcondition> =
            m.getAnnotation(Post::class.java)?.rules
                ?.map { newInstance(it) } ?: emptyList()

        private fun instantiateInvariants(a: Invariants?): List<InvariantRule> =
            a?.rules?.map { newInstance(it) } ?: emptyList()

        private fun <T : Any> newInstance(k: KClass<out T>): T =
            k.java.getDeclaredConstructor().apply { isAccessible = true }.newInstance()
    }
}
