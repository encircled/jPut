package cz.encircled.jput.model;

import cz.encircled.jput.trend.PerformanceTrend;
import org.apache.commons.math3.util.Pair;

import java.util.List;

/**
 * @author Vlad on 27-May-17.
 */
public class MethodTrendConfiguration {

    /**
     * Count of first executions which will be used as a standard for trend measuring
     */
    public int standardSampleSize;

    /**
     * Only result from given percentile are counted for trend tests. Top deviations should be ignored
     */
    public double noisePercentilePercentile;


    // AVERAGE TIME

    public boolean measureAverageTime;

    /**
     * if greater than 0 (in percents), then constant threshold should be used for detecting trends
     */
    public double averageTimeThreshold;

    /**
     * true - if statistic variance of standard sample should be used for detecting trends
     */
    public boolean useAverageTimeVariance;


    // PERCENTILES

    /**
     * percentiles
     */
    public List<Pair<Integer, Double>> percentiles;

    public static MethodTrendConfiguration fromAnnotation(PerformanceTrend conf) {
        return new MethodTrendConfiguration()
                .setAverageTimeThreshold(conf.averageTimeThreshold())
                .setUseAverageTimeVariance(conf.averageTimeVarianceThreshold())
                .setStandardSampleSize(conf.standardSampleSize())
//                .setPercentiles(conf.percentiles())
                ;
    }

    public MethodTrendConfiguration setStandardSampleSize(int standardSampleSize) {
        this.standardSampleSize = standardSampleSize;
        return this;
    }

    public MethodTrendConfiguration setAverageTimeThreshold(double averageTimeThreshold) {
        this.averageTimeThreshold = averageTimeThreshold;
        this.measureAverageTime = true;
        return this;
    }

    public MethodTrendConfiguration setUseAverageTimeVariance(boolean useAverageTimeVariance) {
        this.useAverageTimeVariance = useAverageTimeVariance;
        if (useAverageTimeVariance) {
            this.measureAverageTime = true;
        }
        return this;
    }

    public MethodTrendConfiguration setPercentiles(int[] percentiles) {
//        this.percentiles = percentiles;
        return this;
    }

}
