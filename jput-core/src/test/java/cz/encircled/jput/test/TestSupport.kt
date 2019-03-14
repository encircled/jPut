package cz.encircled.jput.test

import cz.encircled.jput.context.context
import cz.encircled.jput.model.PerfTestExecution
import java.lang.reflect.Method

/**
 * @author Vlad on 27-May-17.
 */
object TestSupport {

    fun getTestExecution(vararg times: Long): PerfTestExecution {
        val run = PerfTestExecution(mapOf("id" to context.executionId))
        run.executionResult = times.toMutableList()
        return run
    }

    fun getTestExecution(method: Method, vararg times: Long): PerfTestExecution {
        val run = PerfTestExecution(mapOf("id" to context.executionId))
        run.executionResult = times.toMutableList()
        run.testId = method.declaringClass.name + "#" + method.name
        return run
    }

}
