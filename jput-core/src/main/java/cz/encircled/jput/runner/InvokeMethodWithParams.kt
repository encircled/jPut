package cz.encircled.jput.runner

import cz.encircled.jput.runner.PutTestExecutorForJUnitRunner.Companion.jPut
import cz.encircled.jput.runner.PutTestExecutorForJUnitRunner.Companion.result
import org.junit.internal.runners.statements.InvokeMethod
import org.junit.runners.model.FrameworkMethod

/**
 * JUnit method invoker referenced by [PutTestExecutorForJUnitRunner]
 */
class InvokeMethodWithParams(private val target: Any, val method: FrameworkMethod) : InvokeMethod(method, target) {

    override fun evaluate() {
        result.set(
                if (method.method.parameterCount > 0) {
                    method.invokeExplosively(target, jPut.get())
                } else {
                    method.invokeExplosively(target)
                }
        )
    }

}