package cz.encircled.jput.context

import cz.encircled.jput.annotation.PerformanceTest
import cz.encircled.jput.annotation.PerformanceTrend
import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.model.TrendTestConfiguration
import java.lang.reflect.Method

fun Int.toPercentile(): Double = this / 100.0

object ConfigurationBuilder {

    fun defaultTestId(method: Method): String = "${method.declaringClass.simpleName}#${method.name}"

    fun buildConfig(conf: PerformanceTest, method: Method): PerfTestConfiguration =
            fromContextParams(fromAnnotation(conf, method))

    // TODO trends via props?
    private fun fromContextParams(conf: PerfTestConfiguration): PerfTestConfiguration {
        val p = JPutContext.PROP_TEST_CONFIG + conf.testId

        val repeats = getOptionalProperty<Int>("$p.repeats") ?: conf.repeats
        val warmUp = getOptionalProperty<Int>("$p.warmUp") ?: conf.warmUp
        val delay = getOptionalProperty<Long>("$p.delay") ?: conf.delay
        val maxTimeLimit = getOptionalProperty<Long>("$p.maxTimeLimit") ?: conf.maxTimeLimit
        val averageTimeLimit = getOptionalProperty<Long>("$p.averageTimeLimit") ?: conf.avgTimeLimit
        val parallel = getOptionalProperty<Int>("$p.parallel") ?: conf.parallelCount
        val rampUp = getOptionalProperty<Long>("$p.rampUp") ?: conf.rampUp
        val maxAllowedExceptionsCount = getOptionalProperty<Long>("$p.maxAllowedExceptionsCount")
                ?: conf.maxAllowedExceptionsCount
        val percentiles = getOptionalMapProperty("$p.percentiles") ?: conf.percentiles

        return conf.copy(repeats = repeats, warmUp = warmUp, delay = delay, rampUp = rampUp,
                maxTimeLimit = maxTimeLimit, avgTimeLimit = averageTimeLimit, percentiles = percentiles,
                parallelCount = parallel, maxAllowedExceptionsCount = maxAllowedExceptionsCount)
    }

    private fun fromAnnotation(conf: PerformanceTest, method: Method): PerfTestConfiguration {
        val trendConfig =
                if (conf.trends.isNotEmpty()) fromAnnotation(conf.trends[0])
                else null

        val testId = if (conf.testId.isBlank()) defaultTestId(method)
        else conf.testId

        val percentiles = conf.percentiles.associate { it.rank.toPercentile() to it.max }

        val methodConfiguration = PerfTestConfiguration(testId, conf.warmUp, conf.repeats, conf.delay,
                conf.maxTimeLimit, conf.averageTimeLimit, percentiles, conf.parallel, conf.rampUp,
                conf.isReactive, conf.maxAllowedExceptionsCount, conf.continueOnException, trendConfig)

        return methodConfiguration.valid()
    }

    private fun fromAnnotation(conf: PerformanceTrend): TrendTestConfiguration =
            TrendTestConfiguration(
                    conf.sampleSize,
                    conf.averageTimeThreshold,
                    conf.useStandardDeviationAsThreshold,
                    conf.sampleSelectionStrategy,
                    conf.noisePercentile.toPercentile()
            )

}