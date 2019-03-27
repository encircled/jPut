package cz.encircled.jput.model

/**
 * Represents the validation result of a performance test.
 * If a test violates some of it's performance constraints, it will be added to [violations] field.
 *
 * Field [params] may contain additional params, which may be required by error message builder later on
 */
data class PerfTestResult(
        val execution: PerfTestExecution,
        val violations: List<PerfConstraintViolation> = emptyList(),
        val params: Map<String, Any> = emptyMap()) {

    val isError: Boolean
        get() = violations.isNotEmpty()

    override fun toString(): String =
            violations.joinToString("\n") {
                it.messageProducer.invoke(this)
            }

}

/**
 * Enumeration of possible perf constraint violations
 */
enum class PerfConstraintViolation(val messageProducer: (result: PerfTestResult) -> String) {

    /**
     * Unit test, avg execution time
     */
    UNIT_AVG({
        "Limit avg time = ${it.execution.conf.avgTimeLimit} ms, actual avg time = ${it.execution.executionAvg} ms"
    }),

    /**
     * Unit test, max execution time
     */
    UNIT_MAX({
        "Limit max time = ${it.execution.conf.maxTimeLimit} ms, actual max time = ${it.execution.executionMax} ms"
    }),

    /**
     * Trend test, avg execution time
     */
    TREND_AVG({
        "Limit avg time = ${it.params["avgLimit"]} ms, actual avg time = ${it.execution.executionAvg} ms"
    })

}