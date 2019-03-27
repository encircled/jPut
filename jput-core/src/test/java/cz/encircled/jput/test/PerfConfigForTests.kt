package cz.encircled.jput.test

import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.model.PerfTestResult
import cz.encircled.jput.model.TrendTestConfiguration
import kotlin.test.assertFalse
import kotlin.test.assertTrue

interface PerfConfigForTests {

    fun configWithTrend(trendTestConfiguration: TrendTestConfiguration): PerfTestConfiguration =
            PerfTestConfiguration(0, 1, 0, 0, trendTestConfiguration)

    fun assertAvgNotValid(result: PerfTestResult) {
        assertTrue(result.isError, result.toString())
    }

    fun assertValid(result: PerfTestResult) {
        assertFalse(result.isError, result.toString())
    }

}