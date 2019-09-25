package cz.encircled.jput

import cz.encircled.jput.context.context
import cz.encircled.jput.model.*
import kotlin.test.assertTrue

interface ShortcutsForTests {

    /**
     * Set properties before test, invoke test, rollback property values
     */
    fun testWithProps(vararg props: Pair<String, String>, testFun: () -> Unit) {
        val originalProps = mutableMapOf<String, String?>()
        props.forEach {
            originalProps[it.first] = System.getProperty(it.second)
            System.setProperty(it.first, it.second)
        }
        try {
            testFun.invoke()
        } finally {
            originalProps.forEach {
                System.setProperty(it.key, it.value ?: "")
            }
        }
    }

    fun getTestExecution(config: PerfTestConfiguration, vararg times: Long): PerfTestExecution {
        val startTime = System.nanoTime()
        val execution = PerfTestExecution(config, mutableMapOf("id" to context.executionId), startTime)

        val repeats = times.mapIndexed { i, time -> i.toLong() to ExecutionRepeat(execution, 1000000L, time) }
                .toMap().toMutableMap()

        execution.executionResult.putAll(repeats)

        return execution
    }

    fun baseConfig() = configWithTrend(null)

    fun configWithTrend(trendTestConfiguration: TrendTestConfiguration?): PerfTestConfiguration =
            PerfTestConfiguration("1", maxTimeLimit = 300, avgTimeLimit = 300, trendConfiguration = trendTestConfiguration)

    fun assertNotValid(expected: PerfConstraintViolation, violations: List<PerfConstraintViolation>) {
        assertTrue(violations.contains(expected), "Expected to be $expected, actual is $violations")
    }

    fun assertValid(violations: List<PerfConstraintViolation>) {
        assertTrue(violations.isEmpty(), "Actual is $violations")
    }

}