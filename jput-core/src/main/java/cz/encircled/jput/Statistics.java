package cz.encircled.jput;

import cz.encircled.jput.model.PerformanceTestRun;

import java.util.stream.LongStream;

/**
 * @author Vlad on 27-May-17.
 */
public class Statistics {

    public static double getAverage(long... input) {
        double sum = 0;
        for (long a : input) {
            sum += a;
        }
        return sum / input.length;
    }

    public static double getVariance(long... input) {
        double average = getAverage(input);
        double temp = 0;
        for (double a : input)
            temp += (a - average) * (a - average);
        return temp / input.length;
    }

    public static double getStandardDeviation(long... input) {
        return Math.sqrt(getVariance(input));
    }

    public static long round(double value) {
        return Math.round(value);
    }

    /**
     * @param input      <b>ordered</b> array
     * @param percentile target percentile
     * @return values below <code>percentile</code>
     */
    public static long[] getPercentile(long[] input, double percentile) {
        JPutCommons.validatePercentile(percentile);

        int upperBound = (int) Math.round(input.length * percentile);

        long[] result = new long[upperBound];
        System.arraycopy(input, 0, result, 0, upperBound);

        return result;
    }

    public static long maxRunTime(PerformanceTestRun run) {
        return LongStream.of(run.runs).max().orElseThrow(IllegalStateException::new);
    }

    public static long averageRunTime(PerformanceTestRun run) {
        return Math.round(LongStream.of(run.runs).average().orElseThrow(IllegalStateException::new));
    }

}
