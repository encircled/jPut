package cz.encircled.jput.annotation

import cz.encircled.jput.trend.SelectionStrategy

/**
 * Configuration for performance trend analysis, must be defined through [PerformanceTest]
 *
 * @author Vlad on 27-May-17.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class PerformanceTrend(

        /**
         * Sample size which is used for trend analysis,
         * i.e. only specified count of previous test executions will be used for analysis
         */
        val sampleSize: Int = 30,

        /**
         * Defines the way, how the sample (a subset of results) should be chosen from the all available previous results
         */
        val sampleSelectionStrategy: SelectionStrategy = SelectionStrategy.USE_LATEST,

        /**
         * Static average time threshold.
         *
         * Performance trend test will fail if average execution time is greater than sample average time plus given threshold
         */
        val averageTimeThreshold: Double = 0.0,

        /**
         * if true - use the sample standard deviation as an average time threshold
         *
         * Performance trend test will fail if average execution time is greater than sample average time + threshold
         */
        val useStandardDeviationAsThreshold: Boolean = false,

        /**
         * Only result from given percentile are counted for trend tests, thus ignoring highest deviations
         */
        val noisePercentile: Int = 0

)