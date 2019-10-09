package cz.encircled.jput.analyzer

import cz.encircled.jput.ShortcutsForTests
import cz.encircled.jput.model.PerfConstraintViolation
import cz.encircled.jput.runner.JPutJUnit4Runner
import cz.encircled.jput.unit.TestExceptionsAnalyzer
import cz.encircled.jput.unit.UnitPerformanceAnalyzer
import cz.encircled.jput.unit.UnitPerformanceAnalyzerImpl
import org.junit.runner.RunWith
import kotlin.test.Test

@RunWith(JPutJUnit4Runner::class)
class UnitAnalyzerTest : ShortcutsForTests {

    private val analyzer: UnitPerformanceAnalyzer = UnitPerformanceAnalyzerImpl()

    @Test
    fun testAverageNotSet() {
        val conf = baseConfig().copy(avgTimeLimit = 0L)
        val testRun = getTestExecution(conf, 99, 101)

        assertValid(analyzer.analyzeUnitTrend(testRun))
    }

   @Test
    fun testPositiveAverage() {
        val conf = baseConfig().copy(avgTimeLimit = 101L)
        val testRun = getTestExecution(conf, 99, 101)

        assertValid(analyzer.analyzeUnitTrend(testRun))
    }

    @Test
    fun testNegativeAverage() {
        val conf = baseConfig().copy(avgTimeLimit = 99L)
        val testRun = getTestExecution(conf, 99, 101)

        assertNotValid(PerfConstraintViolation.UNIT_AVG, analyzer.analyzeUnitTrend(testRun))
    }

    @Test
    fun testMaxNotSet() {
        val conf = baseConfig().copy(maxTimeLimit = 0L)
        val testRun = getTestExecution(conf, 99, 101)

        assertValid(analyzer.analyzeUnitTrend(testRun))
    }

    @Test
    fun testPositiveMax() {
        val conf = baseConfig().copy(maxTimeLimit = 101L)
        val testRun = getTestExecution(conf, 99, 101)

        assertValid(analyzer.analyzeUnitTrend(testRun))
    }

    @Test
    fun testNegativeMax() {
        val conf = baseConfig().copy(maxTimeLimit = 100L)
        val testRun = getTestExecution(conf, 99, 101)

        assertNotValid(PerfConstraintViolation.UNIT_MAX, analyzer.analyzeUnitTrend(testRun))
    }

    @Test
    fun testValidExceptionsCount() {
        val conf = baseConfig().copy(maxAllowedExceptionsCount = 2L)
        val testRun = getTestExecution(conf, 99, 101, 102, 104)
        testRun.executionResult[0]!!.resultDetails.errorMessage = "test"
        testRun.executionResult[1]!!.resultDetails.errorMessage = "test"

        assertValid(TestExceptionsAnalyzer().analyzeUnitTrend(testRun))
    }

    @Test
    fun testNotValidExceptionsCount() {
        val conf = baseConfig().copy(maxAllowedExceptionsCount = 1L)
        val testRun = getTestExecution(conf, 99, 101, 102, 104)
        testRun.executionResult[0]!!.resultDetails.errorMessage = "test"
        testRun.executionResult[1]!!.resultDetails.errorMessage = "test"

        assertNotValid(PerfConstraintViolation.EXCEPTIONS_COUNT, TestExceptionsAnalyzer().analyzeUnitTrend(testRun))
    }

}