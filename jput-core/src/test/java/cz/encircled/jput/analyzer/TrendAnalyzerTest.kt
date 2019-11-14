package cz.encircled.jput.analyzer

import cz.encircled.jput.ShortcutsForTests
import cz.encircled.jput.model.PerfConstraintViolation
import cz.encircled.jput.model.TrendTestConfiguration
import cz.encircled.jput.runner.JPutJUnit4Runner
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

/**
 * @author Vlad on 27-May-17.
 */
@RunWith(JPutJUnit4Runner::class)
class TrendAnalyzerTest : ShortcutsForTests {

    private val trendAnalyzer = SampleBasedTrendAnalyzer()

    @Test
    fun testCollectRuns() {
        val config = baseConfig()
        val runs = trendAnalyzer.collectRuns(listOf(
                getTestExecution(config, 100, 103),
                getTestExecution(config, 102),
                getTestExecution(config, 104)))

        assertEquals(listOf<Long>(100, 103, 102, 104), runs)
    }

    @Test
    fun testPositiveAverageByDeviation() {
        val conf = configWithTrend(TrendTestConfiguration(3, useStandardDeviationAsThreshold = true))
        val testRun = getTestExecution(conf, 100)
        assertValid(trendAnalyzer.analyzeTestTrend(testRun, listOf(100, 100, 100)))
        assertValid(trendAnalyzer.analyzeTestTrend(testRun, listOf(300, 310, 330)))

        // Avg 98.5, var - 1.5
        assertValid(trendAnalyzer.analyzeTestTrend(testRun, listOf(100, 96, 99, 99)))
    }

    @Test
    fun testPositiveDeviationAndStatic() {
        val conf = configWithTrend(TrendTestConfiguration(3, averageTimeThreshold = 5.0, useStandardDeviationAsThreshold = true))
        val testRun = getTestExecution(conf, 105)
        assertValid(trendAnalyzer.analyzeTestTrend(testRun, listOf(90, 100)))
    }

    @Test
    fun testNegativeDeviationAndStatic() {
        val conf = configWithTrend(TrendTestConfiguration(3, averageTimeThreshold = 4.0, useStandardDeviationAsThreshold = true))
        val testRun = getTestExecution(conf, 105)
        assertNotValid(PerfConstraintViolation.TREND_AVG, trendAnalyzer.analyzeTestTrend(testRun, listOf(90, 100)))
    }

    @Test
    fun testNegativeByVariance() {
        // Avg 98.5 ms, variance 2.25 ms
        val conf = configWithTrend(TrendTestConfiguration(4, useStandardDeviationAsThreshold = true))

        val result = trendAnalyzer.analyzeTestTrend(getTestExecution(conf, 102), listOf(100, 96, 99, 99))
        assertNotValid(PerfConstraintViolation.TREND_AVG, result)
    }

    @Test
    fun testPositiveAverageByThreshold() {
        val conf = configWithTrend(TrendTestConfiguration(3, averageTimeThreshold = 10.0))
        val testRun = getTestExecution(conf, 100)

        assertValid(trendAnalyzer.analyzeTestTrend(testRun, listOf(100, 100, 100)))
        assertValid(trendAnalyzer.analyzeTestTrend(testRun, listOf(300, 310, 330)))

        // 100 Avg + 10 threshold
        assertValid(trendAnalyzer.analyzeTestTrend(getTestExecution(conf, 110), listOf(95, 105)))
    }

    @Test
    fun testValidWhenAverageNotSet() {
        val conf = configWithTrend(TrendTestConfiguration(3, averageTimeThreshold = 0.0))
        val testRun = getTestExecution(conf, 500)

        assertValid(trendAnalyzer.analyzeTestTrend(testRun, listOf(100, 100, 100)))
    }

    @Test
    fun testNegativeAverageByThreshold() {
        // Avg 100, threshold - 10
        val conf = configWithTrend(TrendTestConfiguration(2, averageTimeThreshold = 10.0))
        val result = trendAnalyzer.analyzeTestTrend(getTestExecution(conf, 111), listOf(95, 105))

        assertNotValid(PerfConstraintViolation.TREND_AVG, result)
    }

    @Test
    fun testNoisePercentileApplied() {
        // Avg 100 if 0.9 percentile, threshold - 10
        val conf = configWithTrend(TrendTestConfiguration(10, averageTimeThreshold = 10.0, noisePercentile = 0.9))
        val result = trendAnalyzer.analyzeTestTrend(getTestExecution(conf, 111), listOf(95, 105, 95, 105, 95, 105, 95, 105, 95, 1000))

        assertNotValid(PerfConstraintViolation.TREND_AVG, result)
    }

}
