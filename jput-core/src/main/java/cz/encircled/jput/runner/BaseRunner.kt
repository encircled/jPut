package cz.encircled.jput.runner

import cz.encircled.jput.context.context
import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.model.PerfTestExecution
import junit.framework.AssertionFailedError

/**
 * Executes particular piece of code (junit test, or any other function) and runs performance and trend assertions.
 */
open class BaseTestExecutor {

    fun executeTest(config: PerfTestConfiguration, statement: () -> Unit) {
        val execution = PerfTestExecution(config, mapOf("id" to context.executionId))

        val result = performExecution(config, statement)

        execution.executionResult.addAll(result)
        analyzeExecutionResults(execution, config)
        context.resultRecorders.forEach { it.flush() } // TODO
    }

    private fun analyzeExecutionResults(execution: PerfTestExecution, conf: PerfTestConfiguration) {
        val unitViolations = context.unitPerformanceAnalyzer.analyzeUnitTrend(execution)
        if (unitViolations.isError) {
            throw AssertionFailedError("Performance unit test failed.\n$unitViolations")
        }

        if (conf.trendConfiguration != null) {
            // Assume that first has highest priority FIXME when no recorders available
            val sample = context.resultRecorders[0].getSample(execution)

            if (sample.size >= conf.trendConfiguration.sampleSize) {
                val trendViolations = context.trendAnalyzer.analyzeTestTrend(execution, sample)
                if (trendViolations.isError) {
                    throw AssertionFailedError("Performance trend test failed.\n$trendViolations")
                }
            }

            context.resultRecorders.forEach {
                it.appendTrendResult(execution)
            }
        }
    }

    private fun performExecution(conf: PerfTestConfiguration, statement: () -> Unit): List<Long> {
        repeat(conf.warmUp) {
            statement.invoke()
        }

        return (1..conf.repeats).map {
            val start = System.nanoTime()
            statement.invoke()
            (System.nanoTime() - start) / 1000000L
        }
    }

}