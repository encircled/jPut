package cz.encircled.jput.runner

import cz.encircled.jput.JPut
import cz.encircled.jput.JPutImpl
import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.model.RunResult
import org.junit.AssumptionViolatedException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.max


/**
 * Executes particular piece of code (junit test, or any other function) and runs performance and trend assertions.
 */
open class ThreadBasedTestExecutor : BaseTestExecutor() {

    override fun performExecution(execution: PerfTestExecution, statement: (JPut?) -> Any?) {
        val executor = Executors.newScheduledThreadPool(execution.conf.parallelCount)
        val rampUp = rampUpPerThread(execution)

        (1..execution.conf.warmUp).map {
            executor.submit {
                try {
                    statement.invoke(null)
                } catch (e: Exception) {
                    log.warn("Error during test warm up!", e)
                }
            }
        }.forEach { it.get() }

        var scheduledCount = 0
        val repeatsPerThread = max(1, execution.conf.repeats / execution.conf.parallelCount)

        (0 until execution.conf.parallelCount).map { index ->
            // Last thread should take the rest (repeats/parallelCount division fraction)
            val r = if (index == execution.conf.parallelCount - 1) execution.conf.repeats - scheduledCount
            else repeatsPerThread

            scheduledCount += r

            executor.schedule({
                repeat(r) {
                    val repeat = execution.startNextExecution()
                    try {
                        val testResult = statement.invoke(JPutImpl(repeat))
                        if (testResult is RunResult) repeat.resultDetails = testResult
                    } catch (e: Exception) {
                        if (e is AssumptionViolatedException || !execution.conf.continueOnException) {
                            throw e
                        } else {
                            log.debug("Exception occurred during test run", e)
                            repeat.resultDetails = RunResult(500, e)
                        }
                    } finally {
                        repeat.measureElapsed()
                    }

                    if (execution.conf.delay > 0) Thread.sleep(execution.conf.delay)
                }
            }, rampUp * index, TimeUnit.MILLISECONDS)
        }.forEach { it.get() }
    }

}