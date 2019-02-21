package cz.encircled.jput.unit

import cz.encircled.jput.model.MethodConfiguration
import cz.encircled.jput.model.PerformanceTestExecution

import java.lang.reflect.Method

/**
 * @author Vlad on 21-May-17.
 */
interface UnitPerformanceAnalyzer {

    fun buildTestExecution(configuration: MethodConfiguration, method: Method): PerformanceTestExecution

    /**
     * @param elapsedTime - elapsed time in nanoseconds
     */
    fun addTestRun(execution: PerformanceTestExecution, elapsedTime: Long)

    fun analyzeUnitTrend(execution: PerformanceTestExecution, conf: MethodConfiguration): UnitPerformanceResult

    fun buildErrorMessage(result: UnitPerformanceResult, conf: MethodConfiguration): String

}
