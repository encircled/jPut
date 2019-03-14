package cz.encircled.jput.trend

/**
 * @author Vlad on 27-May-17.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class PerformanceTrend(

        /**
         * Sample size which will be used for trend analysis
         */
        val sampleSize: Int = 30,

        /**
         * Defines the strategy for sample selection
         */
        val sampleSelectionStrategy: SelectionStrategy,

        /**
         * Static average time threshold.
         *
         * Performance trend test will fail if average execution time is greater than sample average time plus given threshold
         */
        val averageTimeThreshold: Double = -1.0,

        /**
         * if true - use the sample variance as an average time threshold
         *
         * Performance trend test will fail if average execution time is greater than sample average time plus its variance
         */
        val useSampleVarianceAsThreshold: Boolean = false

)