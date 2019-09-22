package cz.encircled.jput.config

import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.context
import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.model.TrendTestConfiguration
import cz.encircled.jput.trend.PerformanceTrend
import cz.encircled.jput.trend.SelectionStrategy
import cz.encircled.jput.unit.PerformanceTest
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Vlad on 28-May-17.
 */
class AnnotationConfigurationTest {

    @Test
    fun testUnitConfFromAnnotation() {
        val function = this::class.functions.find { it.name == "unitAnnotated" }!!
        val annotation = function.annotations[0] as PerformanceTest

        val config = PerfTestConfiguration.fromAnnotation(annotation, function.javaMethod!!)

        assertEquals(PerfTestConfiguration("AnnotationConfigurationTest#unitAnnotated",
                warmUp = 1, repeats = 2, delay = 100, maxTimeLimit = 100L, avgTimeLimit = 80L, parallelCount = 1
        ), config)
    }

    @Test
    fun testTrendConfFromAnnotation() {
        context = JPutContext()
        context.init()

        val function = this::class.functions.find { it.name == "trendAnnotated" }!!
        val annotation = function.annotations[0] as PerformanceTest

        val config = PerfTestConfiguration.fromAnnotation(annotation, function.javaMethod!!)

        assertEquals(PerfTestConfiguration(testId = "customTestId",
                warmUp = 1, repeats = 2, delay = 100, maxTimeLimit = 100L, avgTimeLimit = 80L, parallelCount = 1,
                trendConfiguration = TrendTestConfiguration(
                        sampleSize = 10, sampleSelectionStrategy = SelectionStrategy.USE_LATEST,
                        averageTimeThreshold = 40.0, useStandardDeviationAsThreshold = true
                )
        ), config)

        // Verify that custom ID is registered
        val id = PerfTestConfiguration.defaultTestId(function.javaMethod!!)
        assertEquals("customTestId", context.customTestIds[id])
    }

    @PerformanceTest(warmUp = 1, repeats = 2, delay = 100, maxTimeLimit = 100L, averageTimeLimit = 80L)
    fun unitAnnotated() {

    }

    @PerformanceTest(testId = "customTestId", warmUp = 1, repeats = 2, delay = 100L, maxTimeLimit = 100L, averageTimeLimit = 80L,
            trends = [PerformanceTrend(
                    sampleSize = 10, sampleSelectionStrategy = SelectionStrategy.USE_LATEST,
                    averageTimeThreshold = 40.0, useStandardDeviationAsThreshold = true
            )])
    fun trendAnnotated() {

    }

}
