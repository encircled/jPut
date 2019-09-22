package cz.encircled.jput.test

import cz.encircled.jput.JPut
import cz.encircled.jput.context.context
import cz.encircled.jput.runner.JPutJUnit4Runner
import org.junit.runner.RunWith
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
        val originalStartTime = execution.startNextExecution()

        context.customTestIds["UserHelpersJPutTest#testMarkPerformanceTestStartWithCustomTestId"] = "customId"
        context.testExecutions["customId"] = execution
        JPut.markPerformanceTestStart()

        // TODO
//        assertTrue(execution.currentExecutionStart.get() > originalStartTime)
    }

    /**
     * Simulate running it from non-JPut function
     */
    @Test
    fun testMarkPerformanceTestStartFromOutside() {
        // should not fail
        JPut.markPerformanceTestStart()
    }

}