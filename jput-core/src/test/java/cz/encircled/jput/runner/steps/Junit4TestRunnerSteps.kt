package cz.encircled.jput.runner.steps

import cz.encircled.jput.JPut
import cz.encircled.jput.annotation.PerformanceTest
import cz.encircled.jput.context.context
import cz.encircled.jput.model.RunResult
import cz.encircled.jput.runner.Conf
import org.junit.Assume
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Ignore
import org.junit.runners.MethodSorters
import org.springframework.test.context.ContextConfiguration
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals

@ContextConfiguration(classes = [Conf::class])
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class Junit4TestRunnerSteps {

    companion object {

        @JvmStatic
        var testCounter = 0

        @JvmStatic
        val parallelCounter = AtomicInteger(0)

        @JvmStatic
        var isBeforeExecuted = false

    }

    @Before
    fun before() {
        isBeforeExecuted = true
    }

    @PerformanceTest
    @Test
    fun testAssumptionFailedPropagated() {
        Assume.assumeTrue(false)
    }

    @PerformanceTest
    @Test
    fun testMarkPerformanceTestStart(jPut: JPut) {
        Thread.sleep(50)
        jPut.markPerformanceTestStart()
    }

    @Ignore // FIXME
    @PerformanceTest
    @Test
    fun testMarkPerformanceTestEnd(jPut: JPut) {
        jPut.markPerformanceTestEnd()
        Thread.sleep(50)
    }

    @PerformanceTest(warmUp = 2, repeats = 2, maxTimeLimit = 5000L)
    @Test
    fun testWarmUpAndRepeatsCount() {
        testCounter++
        assertEquals("testWarmUpAndRepeatsCount", context.currentSuiteMethod!!.name)
    }

    @PerformanceTest(warmUp = 2, repeats = 2, maxTimeLimit = 5000L)
    @Test
    @Ignore
    fun testIgnoredTest() {
    }

    @PerformanceTest(repeats = 1000, parallel = 50, maxAllowedExceptionsCount = 1)
    @Test
    fun testReturnedErrorPropagated(): RunResult {
        val i = parallelCounter.incrementAndGet()
        return RunResult(i, RuntimeException("Test exception $i"))
    }

    @PerformanceTest(repeats = 2, maxAllowedExceptionsCount = 1)
    @Test
    fun testRuntimeExceptionCatched() {
        throw RuntimeException("Test exception")
    }

}