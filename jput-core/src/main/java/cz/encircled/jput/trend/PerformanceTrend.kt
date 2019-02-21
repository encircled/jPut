package cz.encircled.jput.trend

/**
 * @author Vlad on 27-May-17.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class PerformanceTrend(

        /**
         * Count of reference samples picked for trend comparing
         */
        val countOfReferenceSamples: Int = 10,

        /**
         * Defines strategy of picking trend samples
         */
        val trendReferenceStrategy: TrendReferenceStrategy,

        /**
         * Static average time threshold.
         *
         * Performance trend test will fail if average execution time is greater than average time of standard sample plus given threshold
         */
        val averageTimeThreshold: Double = -1.0,

        /**
         * true - use statistic variance of base sample as an average time threshold
         *
         * Performance trend test will fail if average execution time is greater than average time of standard sample plus its variance
         */
        val averageTimeVarianceThreshold: Boolean = false,

        val percentiles: Array<PercentileThreshold> = []
)

enum class TrendReferenceStrategy {

    USE_FIRST,
    USE_LATEST

}