package cz.encircled.jput.runner

import cz.encircled.jput.JPut
import org.junit.internal.runners.statements.InvokeMethod
import org.junit.runners.model.FrameworkMethod

class InvokeMethodWithParams(private val target: Any, val method: FrameworkMethod) : InvokeMethod(method, target) {

    fun evaluateWithParams(jPut: JPut?): Any? {
        return if (method.method.parameterCount > 0) {
            method.invokeExplosively(target, jPut)
        } else {
            method.invokeExplosively(target)
        }
    }

}