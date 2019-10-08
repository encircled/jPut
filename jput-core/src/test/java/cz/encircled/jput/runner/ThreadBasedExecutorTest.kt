package cz.encircled.jput.runner

import cz.encircled.jput.MockRecorder
import cz.encircled.jput.ShortcutsForTests
import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.context
import cz.encircled.jput.model.PerfConstraintViolation
import cz.encircled.jput.model.TrendTestConfiguration
import kotlin.test.*

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
        ThreadBasedTestExecutor().executeTest(baseConfig().copy(repeats = 4, rampUp = 1000, parallelCount = 5)) {
            startTimes.add(System.currentTimeMillis())
        }

        assertTrue(startTimes[4] - startTimes[0] >= 1000)
        assertTrue(startTimes[4] - startTimes[0] < 1100)
        assertTrue(startTimes[4] - startTimes[1] >= 750)
        assertTrue(startTimes[4] - startTimes[2] >= 500)
        assertTrue(startTimes[4] - startTimes[3] >= 250)
    }

}