package cz.encircled.jput.trend

import cz.encircled.jput.Statistics
import cz.encircled.jput.model.PerfConstraintViolation
import cz.encircled.jput.model.PerfTestExecution
import org.slf4j.LoggerFactory

/**
 * @author Vlad on 21-May-17.
 */
interface TrendAnalyzer {

    /**
     *
     * @param execution current test execution
     * @param sample sample of execution elapsed times in ms
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
        var sortedSample: List<Long> = sample.sorted()
        execution.sample.addAll(sortedSample)

        if (trend.noisePercentile > 0) {
            sortedSample = Statistics.getPercentile(sortedSample, trend.noisePercentile)
        }

        val avgThreshold =
                if (trend.useSampleVarianceAsThreshold) Statistics.getVariance(sortedSample)
                else trend.averageTimeThreshold

        if (avgThreshold == 0.0) {
            log.warn("Average time threshold is not set, skipping performance trend test")
            return emptyList()
        }

        val avgLimit = execution.sampleAvg + avgThreshold
        execution.executionParams["avgLimit"] = avgLimit

        return if (execution.executionAvg > avgLimit) listOf(PerfConstraintViolation.TREND_AVG)
        else emptyList()
    }

    fun collectRuns(sample: Collection<PerfTestExecution>): List<Long> =
            sample.map { it.executionResult }.flatten()

}
