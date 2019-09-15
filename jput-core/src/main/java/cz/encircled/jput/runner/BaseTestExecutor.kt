package cz.encircled.jput.runner

import cz.encircled.jput.context.context
import cz.encircled.jput.model.PerfConstraintViolation
import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.model.PerfTestResult
import org.slf4j.LoggerFactory

/**
 * Executes particular piece of code (junit test, or any other function) and runs performance and trend assertions.
 */
open class BaseTestExecutor {

    private val log = LoggerFactory.getLogger(BaseTestExecutor::class.java)

    fun executeTest(config: PerfTestConfiguration, statement: () -> Unit): PerfTestExecution {
        val execution = PerfTestExecution(config, mutableMapOf("id" to context.executionId))

        performExecution(execution, statement)

        try {
            execution.result = analyzeExecutionResults(execution, config)
            return execution
        } finally {
            // TODO error throws must be reworked and throw by runners instead
            writeResults(execution)
        }
    }

    private fun analyzeExecutionResults(execution: PerfTestExecution, conf: PerfTestConfiguration) : PerfTestResult {
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

    private fun performExecution(execution: PerfTestExecution, statement: () -> Unit) {
        context.testExecutions[execution.conf.testId] = execution

        repeat(execution.conf.warmUp) {
            statement.invoke()
        }

        repeat(execution.conf.repeats) {
            execution.startNextExecution()
            statement.invoke()
            execution.finishExecution()
        }

    }

}