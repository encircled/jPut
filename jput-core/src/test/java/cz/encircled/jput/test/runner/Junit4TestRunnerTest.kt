package cz.encircled.jput.test.runner

import cz.encircled.jput.runner.JPutJUnit4Runner
import cz.encircled.jput.test.ShortcutsForTests
import cz.encircled.jput.unit.PerformanceTest
import org.junit.FixMethodOrder
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Verify that [JPutJUnit4Runner] is executing [PerformanceTest]
 */
@RunWith(JPutJUnit4Runner::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class Junit4TestRunnerTest : ShortcutsForTests {

    companion object {
        @JvmStatic
        private var testCounter = 0

        @JvmStatic
        private var ignoredTestCalled = false

    }

    @PerformanceTest(warmUp = 2, repeats = 2, maxTimeLimit = 5000L)
    @Test
    fun baseTest() {
        testCounter++
        println(testCounter)
    }

    @PerformanceTest(warmUp = 2, repeats = 2, maxTimeLimit = 5000L)
    @Test
    @Ignore
    fun ignoredTest() {
        ignoredTestCalled = true
    }

    /**
     * "z" to be last in order
     */
    @Test
    fun z_verify() {
        assertEquals(4, testCounter)
        assertFalse(ignoredTestCalled)
    }

}