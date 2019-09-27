package cz.encircled.jput.runner

import cz.encircled.jput.MockRecorder
import cz.encircled.jput.ShortcutsForTests
import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.context
import cz.encircled.jput.model.PerfConstraintViolation
import cz.encircled.jput.model.TrendTestConfiguration
import org.joda.time.LocalTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Vlad on 15-Sep-19.
 */
class BaseExecutorTest : ShortcutsForTests {

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
        val startTimes = mutableListOf<LocalTime>()
        ThreadBasedTestExecutor().executeTest(baseConfig().copy(repeats = 4, rampUp = 1000, parallelCount = 4)) {
            startTimes.add(LocalTime.now())
        }

        // TODO asserts
        println(startTimes)
    }

}