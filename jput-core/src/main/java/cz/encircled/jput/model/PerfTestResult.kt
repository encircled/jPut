package cz.encircled.jput.model

/**
 * Represents the execution result of a performance test.
 * If a test violates any of it's performance constraints, it will be added to the [violations] field.
 *
 */
data class PerfTestResult(val violations: List<PerfConstraintViolation> = emptyList()) {

    val isError: Boolean
        get() = violations.isNotEmpty()

}