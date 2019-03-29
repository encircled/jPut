package cz.encircled.jput.test

import cz.encircled.jput.JPutJUnitRunner
import cz.encircled.jput.unit.UnitPerformanceAnalyzer
import cz.encircled.jput.unit.UnitPerformanceAnalyzerImpl
import org.junit.runner.RunWith
import kotlin.test.Test

@RunWith(JPutJUnitRunner::class)
class UnitAnalyzerTest : PerfConfigForTests {

    private val analyzer: UnitPerformanceAnalyzer = UnitPerformanceAnalyzerImpl()

    @Test
    fun testPositiveAverage() {
        val conf = baseConfig().copy(avgTimeLimit = 101L)
        val testRun = TestSupport.getTestExecution(conf, 99, 101)

        assertValid(analyzer.analyzeUnitTrend(testRun))
    }

    @Test
    fun testNegativeAverage() {
        val conf = baseConfig().copy(avgTimeLimit = 99L)
        val testRun = TestSupport.getTestExecution(conf, 99, 101)

        assertAvgNotValid(analyzer.analyzeUnitTrend(testRun))
    }

    @Test
    fun testPositiveMax() {
        val conf = baseConfig().copy(maxTimeLimit = 101L)
        val testRun = TestSupport.getTestExecution(conf, 99, 101)

        assertValid(analyzer.analyzeUnitTrend(testRun))
    }

    @Test
    fun testNegativeMax() {
        val conf = baseConfig().copy(maxTimeLimit = 100L)
        val testRun = TestSupport.getTestExecution(conf, 99, 101)

        assertAvgNotValid(analyzer.analyzeUnitTrend(testRun))
    }

}