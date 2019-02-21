package cz.encircled.jput.test

import cz.encircled.jput.JPutContext
import cz.encircled.jput.model.PerformanceTestExecution

import java.lang.reflect.Method

/**
 * @author Vlad on 27-May-17.
 */
object TestSupport {

    fun getTestExecution(vararg times: Long): PerformanceTestExecution {
        val run = PerformanceTestExecution()
        run.runs = times
        run.executionId = JPutContext.context.contextExecutionId
        return run
    }

    fun getTestExecution(method: Method, vararg times: Long): PerformanceTestExecution {
        val run = PerformanceTestExecution()
        run.runs = times
        run.executionId = JPutContext.context.contextExecutionId
        run.testMethod = method.name
        run.testClass = method.declaringClass.name
        return run
    }

}
