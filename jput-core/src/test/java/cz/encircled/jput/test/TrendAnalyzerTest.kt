package cz.encircled.jput.test

import cz.encircled.jput.model.MethodTrendConfiguration
import cz.encircled.jput.trend.StandardSampleTrendAnalyzer
import cz.encircled.jput.trend.TrendResult
import org.junit.Assert
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

/**
 * @author Vlad on 27-May-17.
 */
class TrendAnalyzerTest {

    private val trendAnalyzer = StandardSampleTrendAnalyzer()

    @Test
    fun testCollectRuns() {
        val runs = trendAnalyzer.collectRuns(listOf(TestSupport.getTestExecution(100, 103), TestSupport.getTestExecution(102), TestSupport.getTestExecution(104)))
        assertEquals(listOf<Long>(100, 103, 102, 104), runs)
    }

    @Test
    fun testPositiveAverageByVariance() {
        val conf = MethodTrendConfiguration().setUseAverageTimeVariance(true)
        val testRun = TestSupport.getTestExecution(100)
        assertValid(trendAnalyzer.analyzeTestTrend(conf, testRun, listOf(100, 100, 100)))
        assertValid(trendAnalyzer.analyzeTestTrend(conf, testRun, listOf(300, 310, 330)))

        // Avg 98.5, var - 1.5
        assertValid(trendAnalyzer.analyzeTestTrend(conf, testRun, listOf(100, 96, 99, 99)))
    }

    @Test
    fun testNegativeByVariance() {
        assertAvgNotValid(trendAnalyzer.analyzeTestTrend(MethodTrendConfiguration().setUseAverageTimeVariance(true),
                TestSupport.getTestExecution(150), listOf(100, 96, 99, 99)))

        // Avg 98.5, var - 1.5
        assertAvgNotValid(trendAnalyzer.analyzeTestTrend(MethodTrendConfiguration().setUseAverageTimeVariance(true),
                TestSupport.getTestExecution(101), listOf(100, 96, 99, 99)))
    }

    @Test
    fun testPositiveAverageByThreshold() {
        val conf = MethodTrendConfiguration().setAverageTimeThreshold(0.1)
        val testRun = TestSupport.getTestExecution(100)

        assertValid(trendAnalyzer.analyzeTestTrend(conf, testRun, listOf(100, 100, 100)))
        assertValid(trendAnalyzer.analyzeTestTrend(conf, testRun, listOf(300, 310, 330)))

        // Avg 100, threshold - 10%
        assertValid(trendAnalyzer.analyzeTestTrend(conf, TestSupport.getTestExecution(110), listOf(95, 105)))
    }

    @Test
    fun testNegativeAverageByThreshold() {
        val conf = MethodTrendConfiguration().setAverageTimeThreshold(0.1)

        assertAvgNotValid(trendAnalyzer.analyzeTestTrend(conf,
                TestSupport.getTestExecution(150), listOf(100, 100, 100)))

        // Avg 100, threshold - 10%
        assertAvgNotValid(trendAnalyzer.analyzeTestTrend(conf,
                TestSupport.getTestExecution(111), listOf(95, 105)))
    }

    private fun assertAvgNotValid(trendResult: TrendResult) {
        Assert.assertFalse(trendResult.isAverageMet)
    }

    private fun assertValid(trendResult: TrendResult) {
        Assert.assertTrue(trendResult.isAverageMet)
        Assert.assertNull(trendResult.notMetPercentiles)
    }

}
