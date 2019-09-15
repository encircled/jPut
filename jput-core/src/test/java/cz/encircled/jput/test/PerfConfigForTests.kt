package cz.encircled.jput.test

import cz.encircled.jput.context.context
import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.model.PerfTestResult
import cz.encircled.jput.model.TrendTestConfiguration
import kotlin.test.assertFalse
import kotlin.test.assertTrue

interface PerfConfigForTests {

    fun getTestExecution(config: PerfTestConfiguration, vararg times: Long): PerfTestExecution {
        val run = PerfTestExecution(config, mapOf("id" to context.executionId))
        run.executionResult = times.toMutableList()
        return run
    }

    fun baseConfig() = configWithTrend(null)

    fun configWithTrend(trendTestConfiguration: TrendTestConfiguration?): PerfTestConfiguration =
            PerfTestConfiguration("1", 0, 1, 10, 300, 300,
                    trendTestConfiguration)

    fun assertAvgNotValid(result: PerfTestResult) {
        assertTrue(result.isError, result.toString())
    }

    fun assertValid(result: PerfTestResult) {
        assertFalse(result.isError, result.toString())
    }

}