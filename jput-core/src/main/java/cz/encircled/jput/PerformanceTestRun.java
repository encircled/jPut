package cz.encircled.jput;

import java.lang.reflect.Method;
import java.util.stream.LongStream;

/**
 * @author Vlad on 20-May-17.
 */
public class PerformanceTestRun {

    public Method testMethod;

    public long[] runs;

    MethodConfiguration configuration;

    private boolean runsFinished = false;

    private int repeatCounter;

    public PerformanceTestRun(MethodConfiguration configuration) {
        this.configuration = configuration;
        runs = new long[configuration.repeats];
    }

    public void addRun(long elapsedTime) {
        if (runsFinished) {
            throw new IllegalStateException("All runs are done already!");
        }
        runs[repeatCounter++] = elapsedTime;
        runsFinished = repeatCounter == configuration.repeats;
    }

    public long maxRunTime() {
        checkIsReady();

        return LongStream.of(runs).max().orElseThrow(IllegalStateException::new);
    }

    public long averageRunTime() {
        checkIsReady();

        return Math.round(LongStream.of(runs).average().orElseThrow(IllegalStateException::new));
    }

    private void checkIsReady() {
        if(!runsFinished) {
            throw new IllegalStateException("Not all runs are done yet!");
        }
    }

    @Override
    public String toString() {
        String stats;
        if(runsFinished) {
            stats = String.format("[ averageTime = %d ms, maxTime = %d ms]", averageRunTime(), maxRunTime());
        } else {
            stats = "[ run not finished ]";
        }
        return "PerformanceTestRun{" +
                "testMethod = " + testMethod.getName() +
                ", configuration = " + configuration +
                ", run = " + stats +
                '}';
    }
}
