package cz.encircled.jput.annotation

/**
 * Represents percentile rank and its execution time limit
 *
 * @author Vlad on 14-Nov-19.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class Percentile(

        /**
         * Percentile rank, i.e. [95] for 95% percentile
         */
        val rank: Int,

        /**
         * Upper limit for test execution time in milliseconds
         */
        val max: Long

)

