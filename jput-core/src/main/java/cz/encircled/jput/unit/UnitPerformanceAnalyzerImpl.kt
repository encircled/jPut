package cz.encircled.jput.unit

import cz.encircled.jput.context.context
import cz.encircled.jput.model.PerfConstraintViolation
import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.model.PerfTestResult
import java.lang.reflect.Method

/**
 * @author Vlad on 21-May-17.
 */
class UnitPerformanceAnalyzerImpl : UnitPerformanceAnalyzer {

    override fun buildTestExecution(configuration: PerfTestConfiguration, method: Method): PerfTestExecution {
        val run = PerfTestExecution(mapOf("id" to context.executionId))
        run.testId = method.declaringClass.name + "#" + method.name
        return run
    }

    override fun analyzeUnitTrend(execution: PerfTestExecution, conf: PerfTestConfiguration): PerfTestResult {
        val result = mutableListOf<PerfConstraintViolation>()
        if (execution.executionAvg > conf.avgTimeLimit) {
            result.add(PerfConstraintViolation.UNIT_AVG)
        }

        if (execution.executionMax > conf.maxTimeLimit) {
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

        return PerfTestResult(execution, conf, result)
    }

}
