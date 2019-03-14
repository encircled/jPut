package cz.encircled.jput.trend

import cz.encircled.jput.model.MethodConfiguration
import cz.encircled.jput.model.MethodTrendConfiguration
import cz.encircled.jput.model.PerfTestExecution

/**
 * @author Vlad on 21-May-17.
 */
interface TrendAnalyzer {

    /**
     *
     * @param execution current test execution
     * @param sample sample of execution elapsed times in ms
     */
    fun analyzeTestTrend(configuration: MethodTrendConfiguration, execution: PerfTestExecution, sample: List<Long>): TrendResult

    fun buildErrorMessage(result: TrendResult, conf: MethodConfiguration): String

}
