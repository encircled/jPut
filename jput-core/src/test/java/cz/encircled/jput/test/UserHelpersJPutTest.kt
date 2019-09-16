package cz.encircled.jput.test

import cz.encircled.jput.JPut
import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.context
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * @author Vlad on 15-Sep-19.
 */
class UserHelpersJPutTest : ShortcutsForTests {

    @Test
    fun testMarkPerformanceTestStart() {
        context = JPutContext()
        context.init()

        val execution = getTestExecution(baseConfig())
        val originalStartTime = execution.startNextExecution()

        context.testExecutions["UserHelpersJPutTest#testMarkPerformanceTestStart"] = execution
        JPut.markPerformanceTestStart()

        assertTrue(execution.currentExecutionStart > originalStartTime)
    }

    @Test
    fun testMarkPerformanceTestStartWithCustomTestId() {
        context = JPutContext()
        context.init()

        val execution = getTestExecution(baseConfig())
        val originalStartTime = execution.startNextExecution()

        context.customTestIds["UserHelpersJPutTest#testMarkPerformanceTestStartWithCustomTestId"] = "customId"
        context.testExecutions["customId"] = execution
        JPut.markPerformanceTestStart()

        assertTrue(execution.currentExecutionStart > originalStartTime)
    }

    /**
     * Simulate running it from non-JPut function
     */
    @Test
    fun testMarkPerformanceTestStartFromOutside() {
        context = JPutContext()
        context.init()

        // should not fail
        JPut.markPerformanceTestStart()
    }

}