package cz.encircled.jput.reporter

import cz.encircled.jput.JPutUtils
import cz.encircled.jput.model.PerfTestExecution

abstract class JPutBaseTextReporter : JPutReporter {

    internal fun buildTextReport(execution: PerfTestExecution): String =
            "Test ${execution.conf.testId}:\n" +
                    "avg: ${execution.executionAvg}ms, max: ${execution.executionMax}ms, " +
                    "50%: ${execution.executionPercentile(0.5)}ms, " +
                    "90%: ${execution.executionPercentile(0.9)}ms, " +
                    "95%: ${execution.executionPercentile(0.95)}ms, " +
                    "99%: ${execution.executionPercentile(0.99)}ms, " +
                    "success count: ${execution.successResults().size}, " +
                    "error count: ${execution.errorResults().size}, " +
                    "total count: ${execution.executionResult.size}\n"

    internal fun buildErrorTextReport(execution: PerfTestExecution): String {
        val errorsCount = execution.errorResults()
                .map { "Code ${it.resultDetails.resultCode}, error: ${JPutUtils.buildErrorMessage(it)}" }
                .groupingBy { it }.eachCount()

        return "Test ${execution.conf.testId} errors:\n" + errorsCount.entries.joinToString("\n") {
            it.key + ". Number of errors ${it.value}"
        }
    }

}