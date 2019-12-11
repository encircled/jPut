package cz.encircled.jput.executor

import cz.encircled.jput.JPut
import cz.encircled.jput.MockRecorder
import cz.encircled.jput.ShortcutsForTests
import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.context
import cz.encircled.jput.model.PerfConstraintViolation
import cz.encircled.jput.model.TrendTestConfiguration
import cz.encircled.jput.runner.BaseTestExecutor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.absoluteValue
import kotlin.test.*

/**
 * Base tests for all impls of [BaseTestExecutor]
 *
 * @author encir on 11-Dec-19.
 */
abstract class BaseGenericExecutorTest : ShortcutsForTests {

    /**
     * Simple counter for tests
     */
    var counter = AtomicLong(0L)

    /**
     * Contains invocations of some test functions and its start time
     */
    val invocationTimes = ConcurrentHashMap<String, Long>()

    @BeforeTest
    fun before() {
        context = JPutContext().init()
        counter.set(0)
    }

    // TODO test for reactive as well
    @Ignore // FIXME
    @Test
    fun `test time based run`() {
        val start = System.currentTimeMillis()
        getExecutor().executeTest(baseConfig().copy(runTime = 30)) {
        }

        assertTrue(System.currentTimeMillis() - start >= 29)
    }

    @Test
    fun testRampUp() {
        getExecutor().executeTest(baseConfig().copy(repeats = 5, rampUp = 400, parallelCount = 5), ::stepWithDelay)

        assertEquals(5, invocationTimes.size)

        assertTrue(invocationDif("0 start", "1 start") > 50, "Actual: ${invocationDif("0 start", "1 start")}")

        // It should be 400,300,200,100 - but java scheduler is not 100% precise, so let here some buffer
        listOf(
                Pair(0, 350),
                Pair(1, 250),
                Pair(2, 150),
                Pair(3, 50)
        ).forEach {
            assertTrue(invocationDif("${it.first} start", "4 start") >= it.second - 2, "Index: ${it.first}, $invocationTimes")
        }

        assertTrue(invocationDif("0 start", "4 start") < 1100)
    }

    @Test
    fun `test warmUp is executed`() {
        getExecutor().executeTest(baseConfig().copy(warmUp = 9, repeats = 1), ::stepWithSimpleCounter)

        assertEquals(10, counter.get())

        getExecutor().executeTest(baseConfig().copy(warmUp = 4, repeats = 1), ::stepWithSimpleCounter)

        assertEquals(15, counter.get())
    }

    @Test
    fun `test result recorder is invoked`() {
        val recorder = MockRecorder()
        context.resultRecorders.add(recorder)

        getExecutor().executeTest(baseConfig().copy(testId = "recorderTest"), ::stepWithSimpleCounter)

        assertEquals(1, recorder.executions.size)
        assertEquals("recorderTest", recorder.executions[0].conf.testId)
    }

    @Test
    fun `test trend analyzer is run`() {
        val recorder = MockRecorder()
        context.resultRecorders.add(recorder)
        recorder.mockSample = listOf(10, 12)

        val config = configWithTrend(TrendTestConfiguration(
                sampleSize = 2,
                averageTimeThreshold = 5.0
        ))

        val result = getExecutor().executeTest(config, ::stepWithDelay)

        assertNotValid(PerfConstraintViolation.TREND_AVG, result.violations)
    }

    @Test
    fun `test unit analyzer is run`() {
        val result = getExecutor().executeTest(baseConfig().copy(maxTimeLimit = 10L), ::stepWithDelay)

        assertNotValid(PerfConstraintViolation.UNIT_MAX, result.violations)
    }

    /**
     * Error must be logged, by test should proceed
     */
    @Test
    fun `error during warmUp`() {
        getExecutor().executeTest(baseConfig().copy(warmUp = 1, repeats = 1), ::stepWithError)

        assertEquals(2, counter.get())
    }

    fun invocationDif(left: String, right: String) = (invocationTimes[left]!! - invocationTimes[right]!!).absoluteValue

    abstract fun getExecutor(): BaseTestExecutor

    abstract fun stepWithSimpleCounter(jput: JPut?): Any

    abstract fun stepWithDelay(jput: JPut?): Any

    abstract fun stepWithError(jput: JPut?): Any

}