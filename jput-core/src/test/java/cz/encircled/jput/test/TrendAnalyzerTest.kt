package cz.encircled.jput.test

import cz.encircled.jput.model.TrendTestConfiguration
import cz.encircled.jput.runner.JPutJUnit4Runner
import cz.encircled.jput.trend.SampleBasedTrendAnalyzer
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

/**
 * @author Vlad on 27-May-17.
 */
@RunWith(JPutJUnit4Runner::class)
class TrendAnalyzerTest : PerfConfigForTests {

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
    fun testPositiveAverageByVariance() {
        val conf = configWithTrend(TrendTestConfiguration(3, useSampleVarianceAsThreshold = true))
        val testRun = getTestExecution(conf, 100)
        assertValid(trendAnalyzer.analyzeTestTrend(testRun, listOf(100, 100, 100)))
        assertValid(trendAnalyzer.analyzeTestTrend(testRun, listOf(300, 310, 330)))

        // Avg 98.5, var - 1.5
        assertValid(trendAnalyzer.analyzeTestTrend(testRun, listOf(100, 96, 99, 99)))
    }

    @Test
    fun testNegativeByVariance() {
        // Avg 98.5 ms, variance 2.25 ms
        val conf = configWithTrend(TrendTestConfiguration(4, useSampleVarianceAsThreshold = true))

        assertAvgNotValid(trendAnalyzer.analyzeTestTrend(getTestExecution(conf, 102), listOf(100, 96, 99, 99)))
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
    fun testNegativeAverageByThreshold() {
        // Avg 100, threshold - 10
        val conf = configWithTrend(TrendTestConfiguration(2, averageTimeThreshold = 10.0))
        assertAvgNotValid(trendAnalyzer.analyzeTestTrend(
                getTestExecution(conf, 111), listOf(95, 105)))
    }

}
