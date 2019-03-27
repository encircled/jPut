package cz.encircled.jput.model

data class PerfTestResult(
        val execution: PerfTestExecution,
        val conf: PerfTestConfiguration,
        val violations: List<PerfConstraintViolation> = emptyList(),
        val params: Map<String, Any> = emptyMap()) {

    val isError: Boolean
        get() = violations.isNotEmpty()

    override fun toString(): String =
            violations.joinToString("\n") {
                it.messageProducer.invoke(this)
            }

}

enum class PerfConstraintViolation(val messageProducer: (result: PerfTestResult) -> String) {

    UNIT_AVG({
        "Limit avg time = ${it.conf.avgTimeLimit} ms, actual avg time = ${it.execution.executionAvg} ms"
    }),

    UNIT_MAX({
        "Limit max time = ${it.conf.maxTimeLimit} ms, actual max time = ${it.execution.executionMax} ms"
    }),

    TREND_AVG({
        "Limit avg time = ${it.params["avgLimit"]} ms, actual avg time = ${it.execution.executionAvg} ms"
    })

}