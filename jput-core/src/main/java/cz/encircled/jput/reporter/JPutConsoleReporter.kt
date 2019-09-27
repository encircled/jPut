package cz.encircled.jput.reporter

import cz.encircled.jput.model.PerfTestExecution
import org.slf4j.LoggerFactory

/**
 * @author Vlad on 22-Sep-19.
 */
class JPutConsoleReporter : JPutReporter {

    val log = LoggerFactory.getLogger(JPutConsoleReporter::class.java)

    override fun beforeClass(clazz: Class<*>) {
        log.info("Starting JPut performance tests for ${clazz.simpleName}")
    }

    override fun beforeTest(execution: PerfTestExecution) {
    }

    override fun afterTest(execution: PerfTestExecution) {
        log.info("Test ${execution.conf.testId}:\n" +
                "avg: ${execution.executionAvg}ms, max: ${execution.executionMax}ms, " +
                "50%: ${execution.executionPercentile(0.5)}ms, " +
                "90%: ${execution.executionPercentile(0.9)}ms, " +
                "95%: ${execution.executionPercentile(0.95)}ms, " +
                "99%: ${execution.executionPercentile(0.99)}ms, " +
                "count: ${execution.executionResult.size}")
    }

    override fun afterClass(clazz: Class<*>) {
    }
}