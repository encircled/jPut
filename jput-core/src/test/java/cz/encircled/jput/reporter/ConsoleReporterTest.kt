package cz.encircled.jput.reporter

import cz.encircled.jput.ShortcutsForTests
import cz.encircled.jput.model.ExecutionRun
import cz.encircled.jput.model.RunResult
import cz.encircled.jput.runner.JPutJUnit4Runner
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

            var i = 0L
            (1..100).reversed().forEach {
                execution.executionResult[i++] = ExecutionRun(execution, 0L, it.toLong())
            }

            listOf(
                    Pair("Error 1", 1),
                    Pair("Error 2", 2),
                    Pair("Error 3", 3)
            ).forEach { p ->
                repeat(p.second) {
                    execution.executionResult[execution.executionResult.size.toLong() + 1] = ExecutionRun(execution, 0L, 10000,
                            RunResult(500 + p.second, RuntimeException(p.first)))
                }
            }

            reporter.beforeClass(this::class.java)
            reporter.beforeTest(execution)
            reporter.afterTest(execution)
            reporter.afterClass(this::class.java)

            val split = out.toString().split("\n")

            assertTrue(split[0].contains("Starting JPut performance tests for ConsoleReporterTest"))
            assertTrue(split[1].contains("Test ConsoleReporterTest:"))
            assertTrue(split[2].contains("avg: 51ms, max: 100ms, 50%: 50ms, 90%: 90ms, 95%: 95ms, 99%: 99ms, success count: 100, error count: 6, total count: 106"), split[2])

            assertTrue(split[4].contains("Test ConsoleReporterTest errors:"))
            assertTrue(split[5].contains("Code 501, error: Error 1. Number of errors 1"))
            assertTrue(split[6].contains("Code 502, error: Error 2. Number of errors 2"))
            assertTrue(split[7].contains("Code 503, error: Error 3. Number of errors 3"))
        } finally {
            System.setOut(oldStream)
        }
    }

}