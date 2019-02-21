package cz.encircled.jput.trend

import cz.encircled.jput.model.MethodConfiguration
import cz.encircled.jput.model.MethodTrendConfiguration
import cz.encircled.jput.model.PerformanceTestExecution

/**
 * @author Vlad on 21-May-17.
 */
interface TrendAnalyzer {

    fun analyzeTestTrend(configuration: MethodTrendConfiguration, testExecution: PerformanceTestExecution, vararg standardSampleRuns: Long): TrendResult

    fun buildErrorMessage(result: TrendResult, conf: MethodConfiguration): String
}
