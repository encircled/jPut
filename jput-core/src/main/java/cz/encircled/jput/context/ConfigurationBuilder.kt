package cz.encircled.jput.context

import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.model.TrendTestConfiguration
import cz.encircled.jput.trend.PerformanceTrend
import cz.encircled.jput.unit.PerformanceTest
import java.lang.reflect.Method

object ConfigurationBuilder {

    fun defaultTestId(method: Method): String = "${method.declaringClass.simpleName}#${method.name}"

    fun buildConfig(conf: PerformanceTest, method: Method): PerfTestConfiguration =
            fromContextParams(fromAnnotation(conf, method))

    private fun fromContextParams(conf: PerfTestConfiguration): PerfTestConfiguration {
        val repeats = getOptionalProperty<Int>(JPutContext.PROP_TEST_CONFIG + "${conf.testId}.repeats") ?: conf.repeats
        val warmUp = getOptionalProperty<Int>(JPutContext.PROP_TEST_CONFIG + "${conf.testId}.warmUp") ?: conf.warmUp
        val delay = getOptionalProperty<Long>(JPutContext.PROP_TEST_CONFIG + "${conf.testId}.delay") ?: conf.delay
        val maxTimeLimit = getOptionalProperty<Long>(JPutContext.PROP_TEST_CONFIG + "${conf.testId}.maxTimeLimit") ?: conf.maxTimeLimit
        val averageTimeLimit = getOptionalProperty<Long>(JPutContext.PROP_TEST_CONFIG + "${conf.testId}.averageTimeLimit") ?: conf.avgTimeLimit
        val parallel = getOptionalProperty<Int>(JPutContext.PROP_TEST_CONFIG + "${conf.testId}.parallel") ?: conf.parallelCount
        val rampUp = getOptionalProperty<Long>(JPutContext.PROP_TEST_CONFIG + "${conf.testId}.rampUp") ?: conf.rampUp
        val maxAllowedExceptionsCount = getOptionalProperty<Long>(JPutContext.PROP_TEST_CONFIG + "${conf.testId}.maxAllowedExceptionsCount")
                ?: conf.maxAllowedExceptionsCount

        return conf.copy(repeats = repeats, warmUp = warmUp, delay = delay, rampUp = rampUp,
                maxTimeLimit = maxTimeLimit, avgTimeLimit = averageTimeLimit,
                parallelCount = parallel, maxAllowedExceptionsCount = maxAllowedExceptionsCount)
    }

    private fun fromAnnotation(conf: PerformanceTest, method: Method): PerfTestConfiguration {
        val trendConfig =
                if (conf.trends.isNotEmpty()) fromAnnotation(conf.trends[0])
                else null

        val testId = if (conf.testId.isBlank()) defaultTestId(method)
        else {
            context.customTestIds[defaultTestId(method)] = conf.testId
            conf.testId
        }

        val methodConfiguration = PerfTestConfiguration(testId, conf.warmUp, conf.repeats, conf.delay,
                conf.maxTimeLimit, conf.averageTimeLimit, conf.parallel, conf.rampUp,
                conf.isReactive, conf.maxAllowedExceptionsCount, conf.continueOnException, trendConfig)

        /*TODO val percentiles = conf.percentiles
        check(percentiles.size % 2 == 0) { "Percentiles parameter count must be even" }
        for (i in 0 until percentiles.size - 1) {
            methodConfiguration.percentiles.put(percentiles[i], percentiles[i + 1]);
        } */

        return methodConfiguration.valid()
    }

    private fun fromAnnotation(conf: PerformanceTrend): TrendTestConfiguration =
            TrendTestConfiguration(
                    conf.sampleSize,
                    conf.averageTimeThreshold,
                    conf.useStandardDeviationAsThreshold,
                    conf.sampleSelectionStrategy
            )

}