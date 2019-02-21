package cz.encircled.jput.unit

import cz.encircled.jput.JPutContext
import cz.encircled.jput.Statistics
import cz.encircled.jput.model.MethodConfiguration
import cz.encircled.jput.model.PerformanceTestExecution

import java.lang.reflect.Method

/**
 * @author Vlad on 21-May-17.
 */
class UnitPerformanceAnalyzerImpl : UnitPerformanceAnalyzer {

    override fun buildTestExecution(configuration: MethodConfiguration, method: Method): PerformanceTestExecution {
        val run = PerformanceTestExecution()
        run.runs = LongArray(configuration.repeats)
        run.testMethod = method.name
        run.testClass = method.declaringClass.name
        run.executionId = JPutContext.context.contextExecutionId
        return run
    }

    private fun toString(execution: PerformanceTestExecution, configuration: MethodConfiguration): String {
        val stats: String
        stats = String.format("[ averageTime = %d ms, maxTime = %d ms]", Statistics.averageExecutionTime(execution), Statistics.maxExecutionTime(execution))
        return "PerformanceTestExecution{" +
                "testMethod = " + execution.testMethod +
                ", configuration = " + configuration +
                ", execution = " + stats +
                '}'.toString()
    }

    override fun addTestRun(execution: PerformanceTestExecution, elapsedTime: Long) {
        execution.runs!![execution.positionCounter] = elapsedTime
        execution.positionCounter++
    }

    override fun analyzeUnitTrend(execution: PerformanceTestExecution, conf: MethodConfiguration): UnitPerformanceResult {
        val result = UnitPerformanceResult()

        val runAvgTime = Statistics.averageExecutionTime(execution)
        if (conf.averageTimeLimit > 0 && runAvgTime > conf.averageTimeLimit) {
            result.isAverageLimitMet = false
            result.runAverageTime = runAvgTime
        }

        val runMaxTime = Statistics.maxExecutionTime(execution)
        if (conf.maxTimeLimit > 0 && runMaxTime > conf.maxTimeLimit) {
            result.isMaxLimitMet = false
            result.runMaxTime = runMaxTime
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
            msg += String.format("\nLimit avg time = %d ms\nActual avg time = %d ms", conf.averageTimeLimit, result.runAverageTime)
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
