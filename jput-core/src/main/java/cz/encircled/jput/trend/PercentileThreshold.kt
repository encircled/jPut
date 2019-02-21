package cz.encircled.jput.trend

/**
 * @author Vlad on 27-May-17.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class PercentileThreshold(

        /**
         * Target percentile (e.g. 70, 90, 95 etc)
         */
        val percentile: Int,
        /**
         * Upper threshold in percents, i.e. allowed variation
         */
        val threshold: Int = 10

)
