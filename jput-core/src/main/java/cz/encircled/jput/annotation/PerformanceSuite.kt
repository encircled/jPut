package cz.encircled.jput.annotation

/**
 * Marker for class which contains multiple performance tests
 *
 * @author Vlad on 21-Sep-19.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class PerformanceSuite(

        /**
         * If true, unit tests in this suite will run in parallel
         */
        val parallel: Boolean = false

)