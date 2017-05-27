package cz.encircled.jput.unit;

import cz.encircled.jput.JPutContext;
import cz.encircled.jput.Statistics;
import cz.encircled.jput.model.MethodConfiguration;
import cz.encircled.jput.model.PerformanceTestRun;

import java.lang.reflect.Method;

/**
 * @author Vlad on 21-May-17.
 */
public class PerformanceAnalyzerImpl implements PerformanceAnalyzer {

    @Override
    public PerformanceTestRun build(MethodConfiguration configuration, Method method) {
        PerformanceTestRun run = new PerformanceTestRun();
        run.runs = new long[configuration.repeats];
        run.testMethod = method.getName();
        run.testClass = method.getDeclaringClass().getName();
        run.executionId = JPutContext.getContext().getContextExecutionId();
        return run;
    }

    @Override
    public String toString(PerformanceTestRun run, MethodConfiguration configuration) {
        String stats;
        stats = String.format("[ averageTime = %d ms, maxTime = %d ms]", Statistics.averageRunTime(run), Statistics.maxRunTime(run));
        return "PerformanceTestRun{" +
                "testMethod = " + run.testMethod +
                ", configuration = " + configuration +
                ", run = " + stats +
                '}';
    }

    @Override
    public void addRun(PerformanceTestRun run, MethodConfiguration configuration, long elapsedTime) {
        run.runs[run.positionCounter++] = elapsedTime;
    }

}
