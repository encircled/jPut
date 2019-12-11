package cz.encircled.jput.runner

import cz.encircled.jput.JPut
import cz.encircled.jput.context.context
import cz.encircled.jput.model.PerfConstraintViolation
import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.model.PerfTestExecution
import org.slf4j.LoggerFactory

/**
 * @author encir on 08-Dec-19.
 */
abstract class BaseTestExecutor {

    val log = LoggerFactory.getLogger(BaseTestExecutor::class.java)

    fun executeTest(config: PerfTestConfiguration, statement: (JPut?) -> Any?): PerfTestExecution {
        val execution = PerfTestExecution(config, mutableMapOf("id" to context.executionId), System.nanoTime())

        context.resultReporters.forEach { it.beforeTest(execution) }

        performExecution(execution, statement)

        execution.violations.addAll(analyzeExecutionResults(execution, config))

        writeResults(execution)
        context.resultReporters.forEach { it.afterTest(execution) }

        return execution
    }

    fun rampUpPerThread(execution: PerfTestExecution): Long {
        return if (execution.conf.rampUp > 0) execution.conf.rampUp / (execution.conf.parallelCount - 1) else 0L
    }

    private fun analyzeExecutionResults(execution: PerfTestExecution, conf: PerfTestConfiguration): List<PerfConstraintViolation> {
        val result = mutableListOf<PerfConstraintViolation>()

        context.unitPerformanceAnalyzers.forEach {
            result.addAll(it.analyzeUnitTrend(execution))
        }

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

    abstract fun performExecution(execution: PerfTestExecution, statement: (JPut?) -> Any?)

}