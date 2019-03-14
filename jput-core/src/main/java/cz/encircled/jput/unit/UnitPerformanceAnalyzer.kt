package cz.encircled.jput.unit

import cz.encircled.jput.model.MethodConfiguration
import cz.encircled.jput.model.PerfTestExecution

import java.lang.reflect.Method

/**
 * @author Vlad on 21-May-17.
 */
interface UnitPerformanceAnalyzer {

    fun buildTestExecution(configuration: MethodConfiguration, method: Method): PerfTestExecution

    /**
     * @param elapsedTimes - list of elapsed times in ms
     */
    fun addTestExecutions(execution: PerfTestExecution, elapsedTimes: List<Long>)

    fun analyzeUnitTrend(execution: PerfTestExecution, conf: MethodConfiguration): UnitPerformanceResult

    fun buildErrorMessage(result: UnitPerformanceResult, conf: MethodConfiguration): String

}
