package cz.encircled.jput.model

import cz.encircled.jput.trend.PerformanceTrend
import org.apache.commons.math3.util.Pair

/**
 * @author Vlad on 27-May-17.
 */
class MethodTrendConfiguration {

    /**
     * Count of first executions which will be used as a standard for trend measuring
     */
    var standardSampleSize: Int = 0

    /**
     * Only result from given percentile are counted for trend tests, thus ignoring highest deviations
     */
    var noisePercentilePercentile: Double = 0.toDouble()


    // AVERAGE TIME

    /**
     *
     */
    var measureAverageTime: Boolean = false

    /**
     * if greater than 0 (in percents), then constant threshold should be used for detecting trends
     */
    var averageTimeThreshold: Double = 0.toDouble()

    /**
     * true - if statistic variance of standard sample should be used for detecting trends
     */
    var useAverageTimeVariance: Boolean = false


    // PERCENTILES

    /**
     * percentiles
     */
    var percentiles: List<Pair<Int, Double>>? = null

    fun setStandardSampleSize(standardSampleSize: Int): MethodTrendConfiguration {
        this.standardSampleSize = standardSampleSize
        return this
    }

    fun setAverageTimeThreshold(averageTimeThreshold: Double): MethodTrendConfiguration {
        this.averageTimeThreshold = averageTimeThreshold
        this.measureAverageTime = true
        return this
    }

    fun setUseAverageTimeVariance(useAverageTimeVariance: Boolean): MethodTrendConfiguration {
        this.useAverageTimeVariance = useAverageTimeVariance
        if (useAverageTimeVariance) {
            this.measureAverageTime = true
        }
        return this
    }

    fun setPercentiles(percentiles: IntArray): MethodTrendConfiguration {
        //        this.percentiles = percentiles;
        return this
    }

    companion object {

        fun fromAnnotation(conf: PerformanceTrend): MethodTrendConfiguration {
            return MethodTrendConfiguration()
                    .setAverageTimeThreshold(conf.averageTimeThreshold)
                    .setUseAverageTimeVariance(conf.averageTimeVarianceThreshold)
                    .setStandardSampleSize(conf.countOfReferenceSamples)
            //  .setPercentiles(conf.percentiles())
        }
    }

}
