package cz.encircled.jput.trend

import cz.encircled.jput.Statistics
import cz.encircled.jput.model.PerfConstraintViolation
import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.model.PerfTestResult

/**
 * @author Vlad on 21-May-17.
 */
interface TrendAnalyzer {

    /**
     *
     * @param execution current test execution
     * @param sample sample of execution elapsed times in ms
     */
    fun analyzeTestTrend(execution: PerfTestExecution, sample: List<Long>): PerfTestResult

}

/**
 * Trend analyzer which compares test execution with sample data from previous executions
 *
 * @author Vlad on 27-May-17.
 */
class SampleBasedTrendAnalyzer : TrendAnalyzer {

    override fun analyzeTestTrend(execution: PerfTestExecution, sample: List<Long>): PerfTestResult {
        val trend = execution.conf.trendConfiguration!!
        var sortedSample: List<Long> = sample.sorted()
        execution.sample.addAll(sortedSample)

        if (trend.noisePercentile > 0) {
            sortedSample = Statistics.getPercentile(sortedSample, trend.noisePercentile)
        }

        val avgThreshold =
                if (trend.useSampleVarianceAsThreshold)
                    Statistics.getVariance(sortedSample)
                else
                    trend.averageTimeThreshold // TODO add validation > 0

        val avgLimit = execution.sampleAvg + avgThreshold

        return if (execution.executionAvg > avgLimit)
            PerfTestResult(execution, listOf(PerfConstraintViolation.TREND_AVG), mapOf("avgLimit" to avgLimit))
        else PerfTestResult(execution)
    }

    fun collectRuns(sample: Collection<PerfTestExecution>): List<Long> =
            sample.map { it.executionResult }.flatten()

}
