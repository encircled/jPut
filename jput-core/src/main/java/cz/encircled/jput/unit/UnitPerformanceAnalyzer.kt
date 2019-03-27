package cz.encircled.jput.unit

import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.model.PerfTestResult
import java.lang.reflect.Method

/**
 * @author Vlad on 21-May-17.
 */
interface UnitPerformanceAnalyzer {

    fun buildTestExecution(configuration: PerfTestConfiguration, method: Method): PerfTestExecution

    fun analyzeUnitTrend(execution: PerfTestExecution, conf: PerfTestConfiguration): PerfTestResult

}
