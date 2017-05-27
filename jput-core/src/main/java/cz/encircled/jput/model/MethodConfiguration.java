package cz.encircled.jput.model;

import cz.encircled.jput.unit.PerformanceTest;

import java.util.HashMap;
import java.util.Map;

/**
 * Performance test configuration of a single junit test method
 *
 * @author Vlad on 20-May-17.
 */
public class MethodConfiguration {

    public int warmUp = 0;

    public int repeats = 1;

    // TODO use 100 percentile instead?
    public long maxTimeLimit = 0L;

    public long averageTimeLimit = 0L;

    public Map<Long, Double> percentiles = new HashMap<>(1);

    public static MethodConfiguration fromAnnotation(PerformanceTest conf) {
        long[] percentiles = conf.percentiles();
        if (percentiles.length % 2 != 0) {
            throw new IllegalStateException("Percentiles parameter count must be even");
        }

        MethodConfiguration methodConfiguration = new MethodConfiguration()
                .setWarmUp(conf.warmUp())
                .setRepeats(conf.repeats())
                .setMaxTimeLimit(conf.maxTimeLimit())
                .setAverageTimeLimit(conf.averageTimeLimit());

        for (int i = 0; i < percentiles.length - 1; i++) {
//            methodConfiguration.percentiles.put(percentiles[i], percentiles[i + 1]); TODO
        }

        return methodConfiguration.valid();
    }

    public MethodConfiguration setWarmUp(int warmUp) {
        this.warmUp = warmUp;
        return this;
    }

    public MethodConfiguration setRepeats(int repeats) {
        this.repeats = repeats;
        return this;
    }

    public MethodConfiguration setMaxTimeLimit(long maxTimeLimit) {
        this.maxTimeLimit = maxTimeLimit;
        return this;
    }

    public MethodConfiguration setAverageTimeLimit(long averageTimeLimit) {
        this.averageTimeLimit = averageTimeLimit;
        return this;
    }

    public MethodConfiguration valid() {
        if (warmUp < 0L) {
            throw new IllegalStateException("WarmUp count must be > 0");
        }
        if (repeats < 1L) {
            throw new IllegalStateException("Repeats count must be > 1");
        }
        for (Long percentile : percentiles.keySet()) {
            if (percentile < 1) {
                throw new IllegalStateException("Percentile value must be > 0");
            }
            if (percentile > 100) {
                throw new IllegalStateException("Percentile value must be < 100");
            }
        }

        return this;
    }

    @Override
    public String toString() {
        return "MethodConfiguration{" +
                "warmUp=" + warmUp + " ms" +
                ", repeats=" + repeats + " ms" +
                ", maxTimeLimit=" + maxTimeLimit + " ms" +
                ", averageTimeLimit=" + averageTimeLimit + " ms" +
                '}';
    }

}
