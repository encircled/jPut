package cz.encircled.jput.test

import cz.encircled.jput.JPutJUnitRunner
import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.unit.UnitPerformanceAnalyzer
import cz.encircled.jput.unit.UnitPerformanceAnalyzerImpl
import org.junit.runner.RunWith
import kotlin.test.Test

@RunWith(JPutJUnitRunner::class)
class UnitAnalyzerTest : PerfConfigForTests {

    private val analyzer: UnitPerformanceAnalyzer = UnitPerformanceAnalyzerImpl()

    @Test
    fun testPositiveAverage() {
        val testRun = TestSupport.getTestExecution(99, 101)
        val conf = PerfTestConfiguration(0, 1, 200L, 101L)

        assertValid(analyzer.analyzeUnitTrend(testRun, conf))
    }

    @Test
    fun testNegativeAverage() {
        val testRun = TestSupport.getTestExecution(99, 101)
        val conf = PerfTestConfiguration(0, 1, 200L, 99L)

        assertAvgNotValid(analyzer.analyzeUnitTrend(testRun, conf))
    }

    @Test
    fun testPositiveMax() {
        val testRun = TestSupport.getTestExecution(99, 101)
        val conf = PerfTestConfiguration(0, 1, 101L, 150L)

        assertValid(analyzer.analyzeUnitTrend(testRun, conf))
    }

    @Test
    fun testNegativeMax() {
        val testRun = TestSupport.getTestExecution(99, 101)
        val conf = PerfTestConfiguration(0, 1, 100L, 150L)

        assertAvgNotValid(analyzer.analyzeUnitTrend(testRun, conf))
    }

}