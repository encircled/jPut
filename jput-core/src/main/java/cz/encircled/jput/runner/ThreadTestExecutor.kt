package cz.encircled.jput.runner

import cz.encircled.jput.context.context
import cz.encircled.jput.model.PerfConstraintViolation
import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.model.PerfTestResult
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors


/**
 * Executes particular piece of code (junit test, or any other function) and runs performance and trend assertions.
 */
open class ThreadTestExecutor {

    private val log = LoggerFactory.getLogger(ThreadTestExecutor::class.java)

    fun executeTest(config: PerfTestConfiguration, statement: () -> Unit): PerfTestExecution {
        val execution = PerfTestExecution(config, mutableMapOf("id" to context.executionId))

        context.resultReporter?.beforeTest(execution)

        context.testExecutions[execution.conf.testId] = execution
        performExecution(execution, statement)

        execution.result = analyzeExecutionResults(execution, config)

        writeResults(execution)
        context.resultReporter?.afterTest(execution)

        return execution
    }

    private fun analyzeExecutionResults(execution: PerfTestExecution, conf: PerfTestConfiguration): PerfTestResult {
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

        return PerfTestResult(result)
    }

    private fun writeResults(execution: PerfTestExecution) {
        context.resultRecorders.forEach {
            it.appendTrendResult(execution)
        }
    }

    open fun performExecution(execution: PerfTestExecution, statement: () -> Unit) {
        val executor = Executors.newFixedThreadPool(execution.conf.parallelCount)

        (1..execution.conf.warmUp).map {
            executor.submit(statement)
        }.map { it.get() }

        (1..execution.conf.repeats).map {
            executor.submit {
                execution.startNextExecution()
                statement.invoke()
                execution.finishExecution()

                if (execution.conf.delay > 0) Thread.sleep(execution.conf.delay)
            }
        }.map { it.get() }

    }

}