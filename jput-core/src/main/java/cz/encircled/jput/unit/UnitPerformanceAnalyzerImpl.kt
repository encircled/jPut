package cz.encircled.jput.unit

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

        //  for (Map.Entry<Long, Long> percentile : conf.percentiles.entrySet()) { TODO
        //      long matchingCount = LongStream.of(execution.runs).filter(time -> time <= percentile.getValue()).count();
        //      int matchingPercents = Math.round(matchingCount * 100 / conf.repeats);
        //      if (matchingPercents < percentile.getKey()) {
        //          String assertMessage = "\nMax time = " + percentile.getValue() + "ms \nexpected percentile = " + percentile.getKey() +
        //                  "%\nActual percentile = " + matchingPercents + "%\n\n";
        //          throw new AssertionFailedError(assertMessage + "Performance test failed, max time is greater then limit: " + analyzer.toString(execution, conf));
        //      }
        //  }

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