package cz.encircled.jput.config

import cz.encircled.jput.annotation.Percentile
import cz.encircled.jput.annotation.PerformanceTest
import cz.encircled.jput.annotation.PerformanceTrend
import cz.encircled.jput.context.ConfigurationBuilder
import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.model.TrendTestConfiguration
import cz.encircled.jput.runner.JPutJUnit4Runner
import cz.encircled.jput.trend.SelectionStrategy
import org.junit.runner.RunWith
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * @author Vlad on 28-May-17.
 */
@RunWith(JPutJUnit4Runner::class)
class ConfigurationBuilderTest {

    @Test
    fun testUnitConfFromAnnotation() {
        val function = this::class.functions.find { it.name == "unitAnnotated" }!!
        val annotation = function.annotations[0] as PerformanceTest

        val config = ConfigurationBuilder.buildConfig(annotation, function.javaMethod!!)

        assertEquals(PerfTestConfiguration("ConfigurationBuilderTest#unitAnnotated",
                warmUp = 1, repeats = 2, delay = 100, maxTimeLimit = 100L, avgTimeLimit = 80L, parallelCount = 1,
                percentiles = mapOf(0.5 to 10L, 0.75 to 15L)
        ), config)
    }

    @Test
    fun testTrendConfFromAnnotation() {
        val function = this::class.functions.find { it.name == "trendAnnotated" }!!
        val annotation = function.annotations[0] as PerformanceTest

        val config = ConfigurationBuilder.buildConfig(annotation, function.javaMethod!!)

        assertEquals(PerfTestConfiguration(testId = "customTestId",
                warmUp = 1, repeats = 2, delay = 100, maxTimeLimit = 100L, avgTimeLimit = 80L, parallelCount = 1,
                trendConfiguration = TrendTestConfiguration(
                        sampleSize = 10, sampleSelectionStrategy = SelectionStrategy.USE_LATEST,
                        averageTimeThreshold = 40.0, useStandardDeviationAsThreshold = true, noisePercentile = 0.95
                )
        ), config)
    }

    @Test
    fun testPropertyFileConfig() {
        System.setProperty("jput.config.test.propsTest.repeats", "10")
        System.setProperty("jput.config.test.propsTest.warmUp", "2")
        System.setProperty("jput.config.test.propsTest.delay", "50")
        System.setProperty("jput.config.test.propsTest.maxTimeLimit", "100")
        System.setProperty("jput.config.test.propsTest.averageTimeLimit", "80")
        System.setProperty("jput.config.test.propsTest.parallel", "2")
        System.setProperty("jput.config.test.propsTest.rampUp", "1000")
        System.setProperty("jput.config.test.propsTest.maxAllowedExceptionsCount", "5")
        System.setProperty("jput.config.test.propsTest.percentiles", "50:200,75:300")

        val function = this::class.functions.find { it.name == "forPropertyFileTest" }!!
        val annotation = function.annotations[0] as PerformanceTest

        val config = ConfigurationBuilder.buildConfig(annotation, function.javaMethod!!)

        assertEquals(PerfTestConfiguration("propsTest",
                warmUp = 2, repeats = 10, delay = 50, maxTimeLimit = 100L, avgTimeLimit = 80L,
                parallelCount = 2, rampUp = 1000L, maxAllowedExceptionsCount = 5,
                percentiles = mapOf(0.5 to 200L, 0.75 to 300L)
        ), config)
    }


    @PerformanceTest(warmUp = 1, repeats = 2, delay = 100, maxTimeLimit = 100L, averageTimeLimit = 80L, percentiles = [
        Percentile(50, 10),
        Percentile(75, 15)
    ])
    fun unitAnnotated() {

    }

    @PerformanceTest(testId = "propsTest")
    fun forPropertyFileTest() {

    }

    @PerformanceTest(testId = "customTestId", warmUp = 1, repeats = 2, delay = 100L, maxTimeLimit = 100L, averageTimeLimit = 80L,
            trends = [PerformanceTrend(
                    sampleSize = 10, sampleSelectionStrategy = SelectionStrategy.USE_LATEST,
                    averageTimeThreshold = 40.0, useStandardDeviationAsThreshold = true, noisePercentile = 95
            )])
    fun trendAnnotated() {

    }

    @Test
    fun testInvalidConfiguration() {
        expectCheckException(PerfTestConfiguration("test", warmUp = -1), "warmUp")
        expectCheckException(PerfTestConfiguration("test", warmUp = -100), "warmUp")

        expectCheckException(PerfTestConfiguration("test", repeats = -10), "repeats")
        expectCheckException(PerfTestConfiguration("test", repeats = 0), "repeats")

        expectCheckException(PerfTestConfiguration("test", trendConfiguration = TrendTestConfiguration(
                sampleSize = -1
        )), "sample")

        expectCheckException(PerfTestConfiguration("test", percentiles = mapOf(-0.1 to 50L)), "percentiles")
        expectCheckException(PerfTestConfiguration("test", percentiles = mapOf(0.5 to -1L)), "percentiles")
        expectCheckException(PerfTestConfiguration("test", percentiles = mapOf(1.1 to 50L)), "percentiles")
    }

    private fun expectCheckException(config: PerfTestConfiguration, attr: String) {
        try {
            config.valid()
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.toLowerCase().contains(attr.toLowerCase()))
            return
        }
        fail("IllegalStateException is expected")
    }

}
