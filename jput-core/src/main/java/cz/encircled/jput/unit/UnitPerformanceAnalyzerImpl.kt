package cz.encircled.jput.unit

import cz.encircled.jput.Statistics
import cz.encircled.jput.context.context
import cz.encircled.jput.model.MethodConfiguration
import cz.encircled.jput.model.PerfTestExecution
import java.lang.reflect.Method

/**
 * @author Vlad on 21-May-17.
 */
class UnitPerformanceAnalyzerImpl : UnitPerformanceAnalyzer {

    override fun buildTestExecution(configuration: MethodConfiguration, method: Method): PerfTestExecution {
        val run = PerfTestExecution(mapOf("id" to context.executionId))
        run.testId = method.declaringClass.name + "#" + method.name
        return run
    }

    private fun toString(execution: PerfTestExecution, configuration: MethodConfiguration): String {
        val stats = String.format("[ averageTime = %d ms, maxTime = %d ms]", Statistics.averageExecutionTime(execution), Statistics.maxExecutionTime(execution))
        return "PerfTestExecution{" +
                "testId= " + execution.testId +
                ", configuration = " + configuration +
                ", execution = " + stats +
                '}'.toString()
    }

    override fun addTestExecutions(execution: PerfTestExecution, elapsedTimes: List<Long>) {
        execution.executionResult.addAll(elapsedTimes)
    }

    // TODO test
    override fun analyzeUnitTrend(execution: PerfTestExecution, conf: MethodConfiguration): UnitPerformanceResult {
        val result = UnitPerformanceResult()

        result.executionAvgTime = Statistics.averageExecutionTime(execution)
        if (result.executionAvgTime > conf.averageTimeLimit) {
            result.isAverageLimitMet = false
        }

        result.runMaxTime = Statistics.maxExecutionTime(execution)
        if (result.runMaxTime > conf.maxTimeLimit) {
            result.isMaxLimitMet = false
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

        return result
    }

    override fun buildErrorMessage(result: UnitPerformanceResult, conf: MethodConfiguration): String {
        var msg = ""
        if (!result.isAverageLimitMet) {
            msg += String.format("\nLimit avg time = %d ms\nActual avg time = %d ms", conf.averageTimeLimit, result.executionAvgTime)
        }
        if (!result.isMaxLimitMet) {
            if (!result.isAverageLimitMet) {
                msg += "\n"
            }
            msg += String.format("\nLimit max time = %d ms\nActual max time = %d ms", conf.maxTimeLimit, result.runMaxTime)
        }
        return msg
    }

}
