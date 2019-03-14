package cz.encircled.jput.model

import cz.encircled.jput.trend.PerformanceTrend

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
    var noisePercentile: Double = 0.toDouble()


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
     * true - if sample variance should be used for trend analysis
     */
    var useSampleVarianceAsThreshold: Boolean = false


    // PERCENTILES

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
        this.useSampleVarianceAsThreshold = useAverageTimeVariance
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
                    .setUseAverageTimeVariance(conf.useSampleVarianceAsThreshold)
                    .setStandardSampleSize(conf.sampleSize)
            //  .setPercentiles(conf.percentiles())
        }
    }

}
