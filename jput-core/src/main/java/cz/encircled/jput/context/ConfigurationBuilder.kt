package cz.encircled.jput.context

import cz.encircled.jput.annotation.PerformanceTest
import cz.encircled.jput.annotation.PerformanceTrend
import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.model.TrendTestConfiguration
import org.joda.time.Period
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder
import java.lang.reflect.Method

fun Int.toPercentile(): Double = this / 100.0

object ConfigurationBuilder {

    private val durationFormatter: PeriodFormatter = PeriodFormatterBuilder()
            .appendHours().appendSuffix("h").appendSeparatorIfFieldsAfter(" ")
            .appendMinutes().appendSuffix("min").appendSeparatorIfFieldsAfter(" ")
            .appendSeconds().appendSuffix("sec")
            .toFormatter()

    /**
     * Builds [PerfTestConfiguration] from an [conf] annotation and available properties as well
     */
    fun buildConfig(conf: PerformanceTest, method: Method): PerfTestConfiguration =
            fromContextParams(fromAnnotation(conf, method))

    private val durationPattern: Regex = Regex("[\\d]+h [\\d]+min [\\d]+sec|[\\d]+min [\\d]+sec|[\\d]+sec")

    /**
     * Parse duration in milliseconds from a [src] string in format like "1h 2min 3sec"
     */
    fun parseDuration(src: String): Long {
        if (src.isBlank()) return 0L

        val regex = durationPattern
        check(src.matches(regex)) { "Duration property [$src] is invalid, must be in format: 1h 1min 1sec" }

        var enriched = src
        if (!enriched.contains("min")) enriched = "0min $enriched"
        if (!enriched.contains("h")) enriched = "0h $enriched"

        val formatter = durationFormatter

        return Period.parse(enriched, formatter).toStandardDuration().millis
    }

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

        val testId = if (conf.testId.isBlank()) defaultTestId(method) else conf.testId

        val percentiles = conf.percentiles.associate { it.rank.toPercentile() to it.max }
        val runDuration = parseDuration(conf.runTime)

        val methodConfiguration = PerfTestConfiguration(
                testId = testId,
                warmUp = conf.warmUp,
                repeats = conf.repeats,
                runTime = runDuration,
                delay = conf.delay,
                maxTimeLimit = conf.maxTimeLimit,
                avgTimeLimit = conf.averageTimeLimit,
                percentiles = percentiles,
                parallelCount = conf.parallel,
                rampUp = conf.rampUp,
                isReactive = conf.isReactive,
                maxAllowedExceptionsCount = conf.maxAllowedExceptionsCount,
                continueOnException = conf.continueOnException,
                trendConfiguration = trendConfig
        )

        return methodConfiguration.valid()
    }

    private fun fromAnnotation(conf: PerformanceTrend): TrendTestConfiguration =
            TrendTestConfiguration(
                    sampleSize = conf.sampleSize,
                    averageTimeThreshold = conf.averageTimeThreshold,
                    useStandardDeviationAsThreshold = conf.useStandardDeviationAsThreshold,
                    sampleSelectionStrategy = conf.sampleSelectionStrategy,
                    noisePercentile = conf.noisePercentile.toPercentile()
            )

    private fun defaultTestId(method: Method): String = "${method.declaringClass.simpleName}#${method.name}"

}