package cz.encircled.jput.runner.steps

import cz.encircled.jput.JPut
import cz.encircled.jput.context.context
import cz.encircled.jput.model.RunResult
import cz.encircled.jput.unit.PerformanceTest
import org.junit.FixMethodOrder
import org.junit.Ignore
import org.junit.Test
import org.junit.runners.MethodSorters
import kotlin.test.assertEquals

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class Junit4TestRunnerSteps {

    companion object {

        @JvmStatic
        var testCounter = 0

    }

    @PerformanceTest
    @Test
    fun testMarkPerformanceTestStart(jPut: JPut) {
        Thread.sleep(50)
        jPut.markPerformanceTestStart()
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

    @PerformanceTest(repeats = 2, maxAllowedExceptionsCount = 1)
    @Test
    fun testReturnedErrorPropagated(): RunResult {
        return RunResult(500, RuntimeException("Test exception"))
    }

    @PerformanceTest(repeats = 2, maxAllowedExceptionsCount = 1)
    @Test
    fun testRuntimeExceptionCatched() {
        throw RuntimeException("Test exception")
    }

}