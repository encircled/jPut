package cz.encircled.jput.trend

import cz.encircled.jput.Statistics
import cz.encircled.jput.model.MethodConfiguration
import cz.encircled.jput.model.MethodTrendConfiguration
import cz.encircled.jput.model.PerfTestExecution

/**
 * Trend analyzer which compares test execution with sample data from previous executions
 *
 * @author Vlad on 27-May-17.
 */
class SampleBasedTrendAnalyzer : TrendAnalyzer {

    override fun analyzeTestTrend(configuration: MethodTrendConfiguration, execution: PerfTestExecution, sample: List<Long>): TrendResult {
        var sortedSample: List<Long> = sample.sorted()

        if (configuration.noisePercentile > 0) {
            sortedSample = Statistics.getPercentile(sortedSample, configuration.noisePercentile)
        }

        val sampleAvg = Statistics.getAverage(sortedSample)

        val avgThreshold =
                if (configuration.useSampleVarianceAsThreshold)
                    Statistics.getVariance(sortedSample)
                else
                    sampleAvg * configuration.averageTimeThreshold // TODO add validation > 0

        val executionAvg = Statistics.getAverage(execution.executionResult) // TODO percentile?
        if (executionAvg > sampleAvg + avgThreshold) {
            val trendResult = TrendResult()
            trendResult.isAverageMet = false
            trendResult.runAverageTime = Statistics.round(executionAvg)
            trendResult.deviation = Statistics.round(avgThreshold)
            trendResult.standardAverage = Statistics.round(sampleAvg)
            return trendResult
        }

        return TrendResult()
    }

    override fun buildErrorMessage(result: TrendResult, conf: MethodConfiguration): String {
        var msg = ""
        if (!result.isAverageMet) {
            msg += String.format("\nStandard avg time = %d ms (%d + %d deviation) \nActual avg time = %d ms",
                    result.standardAverage + result.deviation, result.standardAverage, result.deviation, result.runAverageTime)
        }
        /*if (!result.isMaxLimitMet) {
            if (!result.isAverageLimitMet) {
                msg += "\n";
            }
            msg += String.format("\nLimit max time = %d ms\nActual max time = %d ms", conf.maxTimeLimit, 0);// TODO
        }*/
        return msg
    }

    fun collectRuns(sample: Collection<PerfTestExecution>): List<Long> =
            sample.map { it.executionResult }.flatten()

}
