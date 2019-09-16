package cz.encircled.jput.trend

import cz.encircled.jput.deviation
import cz.encircled.jput.model.PerfConstraintViolation
import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.percentile
import org.slf4j.LoggerFactory

/**
 * @author Vlad on 21-May-17.
 */
interface TrendAnalyzer {

    /**
     * [sample] represents times of previous executions in ms, which is used for trend analysis
     *
     * @param execution current test execution
     */
    fun analyzeTestTrend(execution: PerfTestExecution, sample: List<Long>): List<PerfConstraintViolation>

}

/**
 * Trend analyzer which compares test execution with sample data from previous executions
 *
 * @author Vlad on 27-May-17.
 */
class SampleBasedTrendAnalyzer : TrendAnalyzer {

    private val log = LoggerFactory.getLogger(SampleBasedTrendAnalyzer::class.java)

    override fun analyzeTestTrend(execution: PerfTestExecution, sample: List<Long>): List<PerfConstraintViolation> {
        val trend = execution.conf.trendConfiguration!!
        val sortedSample = if (trend.noisePercentile > 0) {
            sample.sorted().percentile(trend.noisePercentile)
        } else sample.sorted()

        val avgThreshold = if (trend.useStandardDeviationAsThreshold) {
            trend.averageTimeThreshold + sortedSample.deviation()
        } else trend.averageTimeThreshold

        if (avgThreshold <= 0.0) {
            log.warn("Average time threshold is not set, skipping performance trend test")
            return emptyList()
        } else {
            log.info("Average time threshold is $avgThreshold")
        }

        val avgLimit = sortedSample.average() + avgThreshold
        execution.executionParams["avgLimit"] = avgLimit

        return if (execution.executionAvg > avgLimit) listOf(PerfConstraintViolation.TREND_AVG)
        else emptyList()
    }

    fun collectRuns(sample: Collection<PerfTestExecution>): List<Long> =
            sample.map { it.executionResult }.flatten()

}
