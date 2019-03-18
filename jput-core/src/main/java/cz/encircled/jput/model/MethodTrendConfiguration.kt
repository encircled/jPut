package cz.encircled.jput.model

import cz.encircled.jput.trend.PerformanceTrend
import cz.encircled.jput.trend.SelectionStrategy

/**
 * @author Vlad on 27-May-17.
 */
data class MethodTrendConfiguration(

        /**
         * Sample size which will be used for trend analysis
         */
        val sampleSize: Int,

        /**
         * Static average time threshold.
         *
         * Performance trend test will fail if average execution time is greater than sample average time plus given threshold
         */
        val averageTimeThreshold: Double = 0.toDouble(),

        /**
         * if true - use the sample variance as an average time threshold
         *
         * Performance trend test will fail if average execution time is greater than sample average time plus its variance
         */
        val useSampleVarianceAsThreshold: Boolean = false,

        /**
         * Defines the strategy for sample selection
         */
        val sampleSelectionStrategy: SelectionStrategy = SelectionStrategy.USE_FIRST,

        /**
         * Only result from given percentile are counted for trend tests, thus ignoring highest deviations
         */
        val noisePercentile: Double = 0.toDouble()

) {

    companion object {

        fun fromAnnotation(conf: PerformanceTrend): MethodTrendConfiguration =
                MethodTrendConfiguration(
                        conf.sampleSize,
                        conf.averageTimeThreshold,
                        conf.useSampleVarianceAsThreshold,
                        conf.sampleSelectionStrategy
                )

    }

}
