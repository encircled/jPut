package cz.encircled.jput.reporter

import cz.encircled.jput.ShortcutsForTests
import cz.encircled.jput.model.ExecutionRepeat
import cz.encircled.jput.model.PerfConstraintViolation
import cz.encircled.jput.runner.JPutJUnit4Runner
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Vlad on 23-Sep-19.
 */
@RunWith(JPutJUnit4Runner::class)
class AllureReporterTest : ShortcutsForTests {

    companion object {

        @BeforeClass
        @JvmStatic
        fun before() {
            File("allure-results").deleteRecursively()
        }

        @AfterClass
        @JvmStatic
        fun after() {
            File("allure-results").deleteRecursively()
        }

    }

    @Test
    fun testAllureReporter() {
        val reporter = JPutAllureReporter()
        val execution = getTestExecution(baseConfig().copy(testId = "AllureTest"))

        val failedExecution = getTestExecution(baseConfig().copy(testId = "FailedReporterTest", maxTimeLimit = 50L))
        failedExecution.violations.add(PerfConstraintViolation.UNIT_MAX)

        (1..100).forEach {
            execution.executionResult[it.toLong()] = ExecutionRepeat(execution, 0L, it.toLong())
            failedExecution.executionResult[it.toLong()] = ExecutionRepeat(failedExecution, 0L, it.toLong())
        }

        reporter.beforeClass(this::class.java)

        reporter.beforeTest(execution)
        reporter.afterTest(execution)

        reporter.beforeTest(failedExecution)
        reporter.afterTest(failedExecution)

        reporter.afterClass(this::class.java)

        val root = File("allure-results")
        assertTrue(root.exists())

        val nested = root.list()!!
        assertTrue(nested.size == 4)

        val resultFile = File("allure-results/${nested.first { it.endsWith(".json") }}")
        val allureResult = resultFile.readLines().joinToString("\n")
        assertTrue(allureResult.contains("\"name\":\"AllureTest\",\"status\":\"passed\",\"stage\":\"finished\""))
        assertTrue(allureResult.contains("\"name\":\"FailedReporterTest\",\"status\":\"failed\",\"stage\":\"finished\""))

        // Check attachments: 1 contains validation error, 2 contain metrics
        val attachments = nested.filter { it.endsWith(".txt") }.map {
            File("allure-results/$it").readLines().joinToString()
        }
        assertEquals(2, attachments.filter { it == "avg: 51ms, max: 100ms, 50%: 50ms, 90%: 90ms, 95%: 95ms, 99%: 99ms" }.size)
        assertEquals(1, attachments.filter { it == "Limit max time = 50 ms, actual max time = 100 ms" }.size)
    }

}