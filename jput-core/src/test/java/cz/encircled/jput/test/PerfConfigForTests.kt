package cz.encircled.jput.test

import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.model.PerfTestResult
import cz.encircled.jput.model.TrendTestConfiguration
import kotlin.test.assertFalse
import kotlin.test.assertTrue

interface PerfConfigForTests {

    fun baseConfig() = configWithTrend(null)

    fun configWithTrend(trendTestConfiguration: TrendTestConfiguration?): PerfTestConfiguration =
            PerfTestConfiguration("1", 0, 1, 10, 300, 300, trendTestConfiguration)

    fun assertAvgNotValid(result: PerfTestResult) {
        assertTrue(result.isError, result.toString())
    }

    fun assertValid(result: PerfTestResult) {
        assertFalse(result.isError, result.toString())
    }

}