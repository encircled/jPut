package cz.encircled.jput.executor

import cz.encircled.jput.MockRecorder
import cz.encircled.jput.ShortcutsForTests
import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.context
import cz.encircled.jput.model.PerfConstraintViolation
import cz.encircled.jput.model.RunResult
import cz.encircled.jput.model.TrendTestConfiguration
import cz.encircled.jput.runner.ThreadBasedTestExecutor
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.*

/**
 * @author Vlad on 15-Sep-19.
 */
class ThreadBasedExecutorTest : ShortcutsForTests {

    @BeforeTest
    fun before() {
        context = JPutContext()
        context.init()
    }

    @AfterTest
    fun after() {
        context.destroy()
    }

    @Test
    fun testWarmUpRun() {
        var counter = 0
        ThreadBasedTestExecutor().executeTest(baseConfig().copy(warmUp = 9)) {
            counter++
        }

        assertEquals(10, counter)

        ThreadBasedTestExecutor().executeTest(baseConfig().copy(warmUp = 4)) {
            counter++
        }

        assertEquals(15, counter)
    }

    @Test
    fun testUnitAnalyzerIsRun() {
        val result = ThreadBasedTestExecutor().executeTest(baseConfig().copy(maxTimeLimit = 10L)) {
            Thread.sleep(100)
        }

        assertNotValid(PerfConstraintViolation.UNIT_MAX, result.violations)
    }

    @Test
    fun testTrendAnalyzerIsRun() {
        val recorder = MockRecorder()
        context.resultRecorders.add(recorder)
        recorder.mockSample = listOf(10, 12)

        val config = configWithTrend(TrendTestConfiguration(
                sampleSize = 2,
                averageTimeThreshold = 5.0
        ))

        val result = ThreadBasedTestExecutor().executeTest(config) {
            Thread.sleep(40)
        }

        assertNotValid(PerfConstraintViolation.TREND_AVG, result.violations)
    }

    @Test
    fun testRecorderInvoked() {
        val recorder = MockRecorder()
        context.resultRecorders.add(recorder)

        ThreadBasedTestExecutor().executeTest(baseConfig().copy(testId = "recorderTest")) {
        }

        assertEquals(1, recorder.executions.size)
        assertEquals("recorderTest", recorder.executions[0].conf.testId)
    }

    @Test
    fun testRampUp() {
        val startTimes = mutableListOf<Long>()
        ThreadBasedTestExecutor().executeTest(baseConfig().copy(repeats = 5, rampUp = 1000, parallelCount = 5)) {
            startTimes.add(System.currentTimeMillis())
        }

        assertEquals(5, startTimes.size)

        // It should be 1000,750,500,250 - but java scheduler is not 100% precise, so let here some buffer
        listOf(
                Pair(0, 950),
                Pair(1, 700),
                Pair(2, 450),
                Pair(3, 200)
        ).forEach {
            assertTrue(startTimes[4] - startTimes[it.first] >= it.second - 2, "${startTimes[4] - startTimes[it.first]} for $it")
        }

        assertTrue(startTimes[4] - startTimes[0] < 1100)
    }

    @Test
    fun testCorrectRepeatsNumberInParallel() {
        val repeats = AtomicInteger(0)
        ThreadBasedTestExecutor().executeTest(baseConfig().copy(repeats = 5, parallelCount = 2)) {
            repeats.incrementAndGet()
        }

        assertEquals(5, repeats.get())
    }

    @Test
    fun testExceptionHandledByDefault() {
        val result = ThreadBasedTestExecutor().executeTest(baseConfig()) {
            throw RuntimeException("Test")
        }

        val details = result.executionResult.values.first().resultDetails
        assertEquals("Test", details.error!!.message)
        assertEquals(500, details.resultCode)
    }

    @Test
    fun testExceptionRethrown() {
        try {
            ThreadBasedTestExecutor().executeTest(baseConfig().copy(continueOnException = false)) {
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
        val result = ThreadBasedTestExecutor().executeTest(baseConfig().copy(maxTimeLimit = 10L)) {
            RunResult(500, RuntimeException("Test"))
        }

        val details = result.executionResult.values.first().resultDetails
        assertEquals(500, details.resultCode)
        assertEquals("Test", details.errorMessage)
    }

    @Test
    fun testTestErrorResultApplied() {
        val result = ThreadBasedTestExecutor().executeTest(baseConfig().copy(maxTimeLimit = 10L)) {
            RunResult(503, RuntimeException("Test"), "Whoops")
        }

        val details = result.executionResult.values.first().resultDetails
        assertEquals(503, details.resultCode)
        assertEquals("Whoops", details.errorMessage)
        assertEquals("Test", details.error!!.message)
    }

    @Test
    fun testTestSuccessResultApplied() {
        val result = ThreadBasedTestExecutor().executeTest(baseConfig().copy(maxTimeLimit = 10L)) {
            RunResult(200)
        }

        val details = result.executionResult.values.first().resultDetails
        assertEquals(200, details.resultCode)
        assertNull(details.errorMessage)
        assertNull(details.error)
    }

}