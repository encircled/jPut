package cz.encircled.jput.model

import cz.encircled.jput.trend.PerformanceTrend
import cz.encircled.jput.trend.SelectionStrategy

/**
 * Represents the configuration of a single trend performance test
 *
 * @author Vlad on 27-May-17.
 */
data class TrendTestConfiguration(

        /**
         * Sample size which will be used for trend analysis
         */
        val sampleSize: Int,

        /**
         * Static average time threshold.
         *
         * Performance trend test will fail if average execution time is greater than sample average time plus given threshold
         */
        val averageTimeThreshold: Double = 0.0,

        /**
         * if true - use the sample variance as an average time threshold
         *
         * Performance trend test will fail if average execution time is greater than sample average time plus its variance
         */
        val useStandardDeviationAsThreshold: Boolean = false,

        /**
         * Defines the strategy for sample selection
         */
        val sampleSelectionStrategy: SelectionStrategy = SelectionStrategy.USE_FIRST,

        /**
         * Only result from given percentile are counted for trend tests, thus ignoring highest deviations
         */
        val noisePercentile: Double = 0.0

) {

    companion object {

        fun fromAnnotation(conf: PerformanceTrend): TrendTestConfiguration =
                TrendTestConfiguration(
                        conf.sampleSize,
                        conf.averageTimeThreshold,
                        conf.useStandardDeviationAsThreshold,
                        conf.sampleSelectionStrategy
                )

    }

}
