package cz.encircled.jput.analyzer

import cz.encircled.jput.model.PerfConstraintViolation
import cz.encircled.jput.model.PerfTestExecution

/**
 * @author Vlad on 21-May-17.
 */
interface UnitPerformanceAnalyzer {

    fun analyzeUnitTrend(execution: PerfTestExecution): List<PerfConstraintViolation>

}


/**
 * This analyzer verifies that execution times (avg, max etc) are within defined limits.
 *
 * @author Vlad on 21-May-17.
 */
class UnitPerformanceAnalyzerImpl : UnitPerformanceAnalyzer {

    override fun analyzeUnitTrend(execution: PerfTestExecution): List<PerfConstraintViolation> {
        val result = mutableListOf<PerfConstraintViolation>()
        if (execution.conf.avgTimeLimit > 0 && execution.executionAvg > execution.conf.avgTimeLimit) {
            result.add(PerfConstraintViolation.UNIT_AVG)
        }

        if (execution.conf.maxTimeLimit > 0 && execution.executionMax > execution.conf.maxTimeLimit) {
            result.add(PerfConstraintViolation.UNIT_MAX)
        }

        for (p in execution.conf.percentiles) {
            val actual = execution.executionPercentile(p.key)
            if (actual > p.value) {
                result.add(PerfConstraintViolation.UNIT_PERCENTILE)
                execution.executionParams["percentileLimit"] = p.value
                execution.executionParams["percentileRank"] = p.key * 100
                execution.executionParams["percentileActual"] = actual
                break
            }
        }

        return result
    }

}

/**
 * This analyzer verifies that count of occurred exceptions is within defined limits
 */
class TestExceptionsAnalyzer : UnitPerformanceAnalyzer {

    override fun analyzeUnitTrend(execution: PerfTestExecution): List<PerfConstraintViolation> =
            if (execution.exceptionsCount() > execution.conf.maxAllowedExceptionsCount) {
                listOf(PerfConstraintViolation.EXCEPTIONS_COUNT)
            } else emptyList()

}