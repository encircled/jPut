package cz.encircled.jput

import cz.encircled.jput.runner.JPutJUnit4Runner
import cz.encircled.jput.unit.PerformanceTest
import org.junit.runner.RunWith
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Vlad on 15-Sep-19.
 */
@RunWith(JPutJUnit4Runner::class)
class UserHelpersJPutTest : ShortcutsForTests {

    @Test
    @PerformanceTest
    @Ignore // TODO needs to be able to run JUnit Runner from test
    fun testMarkPerformanceTestStart(jPut: JPut) {
        val execution = getTestExecution(baseConfig())
        val originalStartTime = execution.startNextExecution().startTime

        jPut.markPerformanceTestStart()

        assertEquals(1, execution.executionResult.size)

        val repeat = execution.executionResult.values.toList()[0]
        assertTrue(repeat.startTime > originalStartTime)
    }

}