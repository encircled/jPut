package cz.encircled.jput.reporter

import cz.encircled.jput.reporter.JPutConsoleReporter
import cz.encircled.jput.runner.JPutJUnit4Runner
import cz.encircled.jput.ShortcutsForTests
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertTrue


/**
 * @author Vlad on 22-Sep-19.
 */
@RunWith(JPutJUnit4Runner::class)
class ConsoleReporterTest : ShortcutsForTests {

    @Test
    fun testConsoleReporter() {
        val oldStream = System.out
        try {
            val out = java.io.ByteArrayOutputStream()
            System.setOut(java.io.PrintStream(out))

            val reporter = JPutConsoleReporter()
            val execution = getTestExecution(baseConfig().copy(testId = "ConsoleReporterTest"))
            execution.startNextExecution()
            Thread.sleep(20)
            execution.finishExecution()

            reporter.beforeClass(this::class.java)
            reporter.beforeTest(execution)
            reporter.afterTest(execution)
            reporter.afterClass(this::class.java)

            val split = out.toString().split("\n")
            assertTrue(split[0].contains("Starting JPut performance tests for ConsoleReporterTest"))
            assertTrue(split[1].contains("Test ConsoleReporterTest:"))
            assertTrue(split[2].contains("avg:"))
            assertTrue(split[2].contains("max:"))
        } finally {
            System.setOut(oldStream)
        }
    }

}