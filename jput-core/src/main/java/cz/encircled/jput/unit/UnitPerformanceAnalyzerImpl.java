package cz.encircled.jput.unit;

import cz.encircled.jput.JPutContext;
import cz.encircled.jput.Statistics;
import cz.encircled.jput.model.MethodConfiguration;
import cz.encircled.jput.model.PerformanceTestRun;

import java.lang.reflect.Method;

/**
 * @author Vlad on 21-May-17.
 */
public class UnitPerformanceAnalyzerImpl implements UnitPerformanceAnalyzer {

    @Override
    public PerformanceTestRun buildRun(MethodConfiguration configuration, Method method) {
        PerformanceTestRun run = new PerformanceTestRun();
        run.runs = new long[configuration.repeats];
        run.testMethod = method.getName();
        run.testClass = method.getDeclaringClass().getName();
        run.executionId = JPutContext.getContext().getContextExecutionId();
        return run;
    }

    private String toString(PerformanceTestRun run, MethodConfiguration configuration) {
        String stats;
        stats = String.format("[ averageTime = %d ms, maxTime = %d ms]", Statistics.averageRunTime(run), Statistics.maxRunTime(run));
        return "PerformanceTestRun{" +
                "testMethod = " + run.testMethod +
                ", configuration = " + configuration +
                ", run = " + stats +
                '}';
    }

    @Override
    public void addRun(PerformanceTestRun run, long elapsedTime) {
        run.runs[run.positionCounter++] = elapsedTime;
    }

    @Override
    public UnitPerformanceResult analyzeUnitTrend(PerformanceTestRun run, MethodConfiguration conf) {
        UnitPerformanceResult result = new UnitPerformanceResult();

        long runAvgTime = Statistics.averageRunTime(run);
        if (conf.averageTimeLimit > 0 && runAvgTime > conf.averageTimeLimit) {
            result.isAverageLimitMet = false;
            result.runAverageTime = runAvgTime;
        }

        long runMaxTime = Statistics.maxRunTime(run);
        if (conf.maxTimeLimit > 0 && runMaxTime > conf.maxTimeLimit) {
            result.isMaxLimitMet = false;
            result.runMaxTime = runMaxTime;
        }
//                for (Map.Entry<Long, Long> percentile : conf.percentiles.entrySet()) { TODO
//                    long matchingCount = LongStream.of(run.runs).filter(time -> time <= percentile.getValue()).count();
//                    int matchingPercents = Math.round(matchingCount * 100 / conf.repeats);
//                    if (matchingPercents < percentile.getKey()) {
//                        String assertMessage = "\nMax time = " + percentile.getValue() + "ms \nexpected percentile = " + percentile.getKey() +
//                                "%\nActual percentile = " + matchingPercents + "%\n\n";
//                        throw new AssertionFailedError(assertMessage + "Performance test failed, max time is greater then limit: " + analyzer.toString(run, conf));
//                    }
//                }

        return result;
    }

    @Override
    public String buildErrorMessage(UnitPerformanceResult result, MethodConfiguration conf) {
        String msg = "";
        if (!result.isAverageLimitMet) {
            msg += String.format("\nLimit avg time = %d ms\nActual avg time = %d ms", conf.averageTimeLimit, result.runAverageTime);
        }
        if (!result.isMaxLimitMet) {
            if (!result.isAverageLimitMet) {
                msg += "\n";
            }
            msg += String.format("\nLimit max time = %d ms\nActual max time = %d ms", conf.maxTimeLimit, result.runMaxTime);
        }
        return msg;
    }

}
