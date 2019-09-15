package cz.encircled.jput.test

import cz.encircled.jput.context.context
import cz.encircled.jput.model.PerfConstraintViolation
import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.model.TrendTestConfiguration
import kotlin.test.assertTrue

interface ShortcutsForTests {

    fun getTestExecution(config: PerfTestConfiguration, vararg times: Long): PerfTestExecution {
        return PerfTestExecution(config, mutableMapOf("id" to context.executionId), times.toMutableList())
    }

    fun baseConfig() = configWithTrend(null)

    fun configWithTrend(trendTestConfiguration: TrendTestConfiguration?): PerfTestConfiguration =
            PerfTestConfiguration("1", 0, 1, 10, 300, 300,
                    trendTestConfiguration)

    fun assertNotValid(expected: PerfConstraintViolation, violations: List<PerfConstraintViolation>) {
        assertTrue(violations.contains(expected), "Expected to be $expected, actual is [$violations]")
    }

    fun assertValid(violations: List<PerfConstraintViolation>) {
        assertTrue(violations.isEmpty(), "Actual is [$violations]")
    }

}