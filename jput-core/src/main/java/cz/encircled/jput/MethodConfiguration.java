package cz.encircled.jput;

/**
 * Performance test configuration of a single junit test method
 *
 * @author Vlad on 20-May-17.
 */
public class MethodConfiguration {

    public int warmUp = 0;

    public int repeats = 1;

    public long maxTimeLimit = 0L;

    public long averageTimeLimit = 0L;

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

    public static MethodConfiguration fromAnnotation(PerformanceTest conf) {
        return new MethodConfiguration()
                .setWarmUp(conf.warmUp())
                .setRepeats(conf.repeats())
                .setMaxTimeLimit(conf.maxTimeLimit())
                .setAverageTimeLimit(conf.averageTimeLimit())
                .valid();
    }

}
