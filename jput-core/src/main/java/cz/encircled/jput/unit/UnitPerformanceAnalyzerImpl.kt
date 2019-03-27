package cz.encircled.jput.unit

import cz.encircled.jput.model.PerfConstraintViolation
import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.model.PerfTestResult

/**
 * @author Vlad on 21-May-17.
 */
interface UnitPerformanceAnalyzer {

    fun analyzeUnitTrend(execution: PerfTestExecution): PerfTestResult

}


/**
 * @author Vlad on 21-May-17.
 */
class UnitPerformanceAnalyzerImpl : UnitPerformanceAnalyzer {

    override fun analyzeUnitTrend(execution: PerfTestExecution): PerfTestResult {
        val result = mutableListOf<PerfConstraintViolation>()
        if (execution.executionAvg > execution.conf.avgTimeLimit) {
            result.add(PerfConstraintViolation.UNIT_AVG)
        }

        if (execution.executionMax > execution.conf.maxTimeLimit) {
            result.add(PerfConstraintViolation.UNIT_MAX)
        }

        //                for (Map.Entry<Long, Long> percentile : conf.percentiles.entrySet()) { TODO
        //                    long matchingCount = LongStream.of(execution.runs).filter(time -> time <= percentile.getValue()).count();
        //                    int matchingPercents = Math.round(matchingCount * 100 / conf.repeats);
        //                    if (matchingPercents < percentile.getKey()) {
        //                        String assertMessage = "\nMax time = " + percentile.getValue() + "ms \nexpected percentile = " + percentile.getKey() +
        //                                "%\nActual percentile = " + matchingPercents + "%\n\n";
        //                        throw new AssertionFailedError(assertMessage + "Performance test failed, max time is greater then limit: " + analyzer.toString(execution, conf));
        //                    }
        //                }

        return PerfTestResult(execution, result)
    }

}
