package cz.encircled.jput.executor

import cz.encircled.jput.JPut
import cz.encircled.jput.ShortcutsForTests
import cz.encircled.jput.model.RunResult
import cz.encircled.jput.runner.ThreadBasedTestExecutor
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail

/**
 * @author Vlad on 15-Sep-19.
 */
class ThreadBasedExecutorTest : BaseGenericExecutorTest(), ShortcutsForTests {

    override fun stepWithSimpleCounter(jput: JPut?) {
        invocationTimes["${counter.getAndIncrement()} start"] = System.currentTimeMillis()
    }

    override fun stepWithDelay(jput: JPut?) {
        invocationTimes["${counter.getAndIncrement()} start"] = System.currentTimeMillis()
        Thread.sleep(500)
    }

    override fun stepWithError(jput: JPut?) {
        invocationTimes["${counter.getAndIncrement()} start"] = System.currentTimeMillis()
        throw RuntimeException("Test exception")
    }

    @Test
    fun testCorrectRepeatsNumberInParallel() {
        val repeats = AtomicInteger(0)
        getExecutor().executeTest(baseConfig().copy(repeats = 5, parallelCount = 2)) {
            repeats.incrementAndGet()
        }

        assertEquals(5, repeats.get())
    }

    @Test
    fun testExceptionHandledByDefault() {
        val result = getExecutor().executeTest(baseConfig()) {
            throw RuntimeException("Test")
        }

        val details = result.executionResult.values.first().resultDetails
        assertEquals("Test", details.error!!.message)
        assertEquals(500, details.resultCode)
    }

    @Test
    fun testExceptionRethrown() {
        try {
            getExecutor().executeTest(baseConfig().copy(continueOnException = false)) {
                throw RuntimeException("Test")
            }
        } catch (e: Exception) {
            assertEquals("java.lang.RuntimeException: Test", e.message)
            return
        }

        fail("Exception expected")
    }

    @Test
    fun testTestErrorResultAppliedWithMessageFromError() {
        val result = getExecutor().executeTest(baseConfig().copy(maxTimeLimit = 10L)) {
            RunResult(500, RuntimeException("Test"))
        }

        val details = result.executionResult.values.first().resultDetails
        assertEquals(500, details.resultCode)
        assertEquals("Test", details.errorMessage)
    }

    @Test
    fun testTestErrorResultApplied() {
        val result = getExecutor().executeTest(baseConfig().copy(maxTimeLimit = 10L)) {
            RunResult(503, RuntimeException("Test"), "Whoops")
        }

        val details = result.executionResult.values.first().resultDetails
        assertEquals(503, details.resultCode)
        assertEquals("Whoops", details.errorMessage)
        assertEquals("Test", details.error!!.message)
    }

    @Test
    fun testTestSuccessResultApplied() {
        val result = getExecutor().executeTest(baseConfig().copy(maxTimeLimit = 10L)) {
            RunResult(200)
        }

        val details = result.executionResult.values.first().resultDetails
        assertEquals(200, details.resultCode)
        assertNull(details.errorMessage)
        assertNull(details.error)
    }

    override fun getExecutor() = ThreadBasedTestExecutor()
}