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
        val conf = PerfTestConfiguration("1", 0, 1, 200L, 101L)
        val testRun = TestSupport.getTestExecution(conf, 99, 101)

        assertValid(analyzer.analyzeUnitTrend(testRun))
    }

    @Test
    fun testNegativeAverage() {
        val conf = PerfTestConfiguration("1", 0, 1, 200L, 99L)
        val testRun = TestSupport.getTestExecution(conf, 99, 101)

        assertAvgNotValid(analyzer.analyzeUnitTrend(testRun))
    }

    @Test
    fun testPositiveMax() {
        val conf = PerfTestConfiguration("1", 0, 1, 101L, 150L)
        val testRun = TestSupport.getTestExecution(conf, 99, 101)

        assertValid(analyzer.analyzeUnitTrend(testRun))
    }

    @Test
    fun testNegativeMax() {
        val conf = PerfTestConfiguration("1", 0, 1, 100L, 150L)
        val testRun = TestSupport.getTestExecution(conf, 99, 101)

        assertAvgNotValid(analyzer.analyzeUnitTrend(testRun))
    }

}