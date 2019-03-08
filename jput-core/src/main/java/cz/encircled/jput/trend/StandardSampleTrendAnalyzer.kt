package cz.encircled.jput.trend

import cz.encircled.jput.Statistics
import cz.encircled.jput.model.MethodConfiguration
import cz.encircled.jput.model.MethodTrendConfiguration
import cz.encircled.jput.model.PerformanceTestExecution
import java.util.*

/**
 * @author Vlad on 27-May-17.
 */
class StandardSampleTrendAnalyzer : TrendAnalyzer {

    override fun analyzeTestTrend(configuration: MethodTrendConfiguration, testExecution: PerformanceTestExecution, referenceExecutionTimes: List<Long>): TrendResult {
        var standardSampleRuns: List<Long> = referenceExecutionTimes ?: return TrendResult()
        standardSampleRuns.sorted()
        if (configuration.noisePercentilePercentile > 0) {
            standardSampleRuns = Statistics.getPercentile(standardSampleRuns, configuration.noisePercentilePercentile)
        }

        if (configuration.measureAverageTime) {
            val standardAverage = Statistics.getAverage(standardSampleRuns)

            val deviation =
                    if (configuration.useAverageTimeVariance)
                        Statistics.getDeviation(standardSampleRuns)
                    else
                        standardAverage * configuration.averageTimeThreshold // TODO add validation > 0

            val runMean = Statistics.getAverage(testExecution.runs) // TODO percentile?
            if (runMean > standardAverage + deviation) {
                val trendResult = TrendResult()
                trendResult.isAverageMet = false
                trendResult.runAverageTime = Statistics.round(runMean)
                trendResult.deviation = Statistics.round(deviation)
                trendResult.standardAverage = Statistics.round(standardAverage)
                return trendResult
            }
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

    fun collectRuns(referenceExecutions: Collection<PerformanceTestExecution>): List<Long> =
            referenceExecutions.map { it.runs }.flatten()

}
