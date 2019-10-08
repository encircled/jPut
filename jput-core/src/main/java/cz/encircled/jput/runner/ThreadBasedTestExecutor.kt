package cz.encircled.jput.runner

import cz.encircled.jput.context.context
import cz.encircled.jput.model.PerfConstraintViolation
import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.model.PerfTestExecution
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.max


/**
 * Executes particular piece of code (junit test, or any other function) and runs performance and trend assertions.
 */
open class ThreadBasedTestExecutor {

    private val log = LoggerFactory.getLogger(ThreadBasedTestExecutor::class.java)

    fun executeTest(config: PerfTestConfiguration, statement: () -> Unit): PerfTestExecution {
        val execution = PerfTestExecution(config, mutableMapOf("id" to context.executionId), System.nanoTime())

        context.resultReporters.forEach { it.beforeTest(execution) }

        context.testExecutions[execution.conf.testId] = execution
        performExecution(execution, statement)

        execution.violations.addAll(analyzeExecutionResults(execution, config))

        writeResults(execution)
        context.resultReporters.forEach { it.afterTest(execution) }

        return execution
    }

    private fun analyzeExecutionResults(execution: PerfTestExecution, conf: PerfTestConfiguration): List<PerfConstraintViolation> {
        val result = mutableListOf<PerfConstraintViolation>()
        val unitViolations = context.unitPerformanceAnalyzer.analyzeUnitTrend(execution)
        result.addAll(unitViolations)

        if (conf.trendConfiguration != null && context.resultRecorders.isNotEmpty()) {
            // Assume that first has highest priority TODO support main/slave recorders instead
            val sample = context.resultRecorders[0].getSample(execution)

            if (sample.size >= conf.trendConfiguration.sampleSize) {
                val trendViolations = context.trendAnalyzer.analyzeTestTrend(execution, sample)
                result.addAll(trendViolations)
            } else {
                log.info("Skipping performance trend assertions since current sample size is too small [current is ${sample.size}]")
            }
        }

        return result
    }

    private fun writeResults(execution: PerfTestExecution) {
        context.resultRecorders.forEach {
            it.appendTrendResult(execution)
        }
    }

    open fun performExecution(execution: PerfTestExecution, statement: () -> Unit) {
        val executor = Executors.newScheduledThreadPool(execution.conf.parallelCount)
        val rampUp = if (execution.conf.rampUp > 0) execution.conf.rampUp / (execution.conf.parallelCount - 1) else 0L

        (1..execution.conf.warmUp).map {
            executor.submit(statement)
        }.map { it.get() }

        var scheduledCount = 0
        val repeatsPerThread = max(1, execution.conf.repeats / execution.conf.parallelCount)

        (0 until execution.conf.parallelCount).map { index ->
            // Last thread should take the rest (repeats/parallelCount division fraction)
            val r = if (index == execution.conf.parallelCount - 1) execution.conf.repeats - scheduledCount
            else repeatsPerThread

            scheduledCount += r

            executor.schedule({
                repeat(r) {
                    execution.startNextExecution()
                    statement.invoke()
                    execution.getCurrentRun().measureElapsed()

                    if (execution.conf.delay > 0) Thread.sleep(execution.conf.delay)
                }
            }, rampUp * index, TimeUnit.MILLISECONDS)
        }.map { it.get() }
    }

}