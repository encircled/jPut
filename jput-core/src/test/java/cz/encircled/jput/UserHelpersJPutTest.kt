package cz.encircled.jput

import cz.encircled.jput.context.context
import cz.encircled.jput.model.ExecutionRunResultDetails
import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.runner.JPutJUnit4Runner
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Vlad on 15-Sep-19.
 */
@RunWith(JPutJUnit4Runner::class)
class UserHelpersJPutTest : ShortcutsForTests {

    @Test
    fun testMarkPerformanceTestStart() {
        val execution = getTestExecution(baseConfig())
        val originalStartTime = execution.startNextExecution().startTime

        context.testExecutions["UserHelpersJPutTest#testMarkPerformanceTestStart"] = execution
        JPut.markPerformanceTestStart()

        assertEquals(1, execution.executionResult.size)

        val repeat = execution.executionResult.values.toList()[0]
        assertTrue(repeat.startTime > originalStartTime)
    }

    @Test
    fun testMarkPerformanceTestStartWithCustomTestId() {
        val execution = getTestExecution(baseConfig())
        val originalStartTime = execution.startNextExecution().relativeStartTime

        context.customTestIds["UserHelpersJPutTest#testMarkPerformanceTestStartWithCustomTestId"] = "customId"
        context.testExecutions["customId"] = execution
        JPut.markPerformanceTestStart()

        assertTrue(execution.executionResult.values.first().relativeStartTime > originalStartTime)
    }

    /**
     * Simulate running it from non-JPut function
     */
    @Test
    fun testMarkPerformanceTestStartFromOutside() {
        // should not fail
        JPut.markPerformanceTestStart()
    }

    @Test
    fun testSetPerformanceTestResult() {
        val execution = getTestExecution(baseConfig())
        execution.startNextExecution()

        context.testExecutions["UserHelpersJPutTest#testSetPerformanceTestResult"] = execution

        val actual = {
            execution.executionResult.values.first().resultDetails
        }

        val details = ExecutionRunResultDetails(200, RuntimeException(), "testMessage")
        JPut.setPerformanceTestResult(details)
        assertEquals(details, actual())

        JPut.setPerformanceTestResult(403)
        assertEquals(ExecutionRunResultDetails(403), actual())

        val error = RuntimeException("Test")
        JPut.setPerformanceTestResult(error)
        assertEquals(ExecutionRunResultDetails(500, error, "Test"), actual())
    }

    @Test
    fun testMultiThreadSetPerformanceTestResult() {
        val countDown = CountDownLatch(10)

        val execution = getTestExecution(baseConfig())
        context.testExecutions["UserHelpersJPutTest#testMultiThreadSetPerformanceTestResult"] = execution

        (1..10).map {
            Runnable {
                testMultiThreadSetPerformanceTestResult(execution, it, countDown)
            }
        }.forEach {
            Thread(it).start()
        }

        countDown.await()
        val actual = execution.executionResult.values
                .map { it.resultDetails!!.resultCode!! }.sorted()
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), actual)
    }

    private fun testMultiThreadSetPerformanceTestResult(execution: PerfTestExecution, it: Int, countDown: CountDownLatch) {
        try {
            execution.startNextExecution()
            Thread.sleep(20)
            JPut.setPerformanceTestResult(ExecutionRunResultDetails(it))
        } finally {
            countDown.countDown()
        }
    }

}