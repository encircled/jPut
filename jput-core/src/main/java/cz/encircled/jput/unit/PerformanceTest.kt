package cz.encircled.jput.unit

import cz.encircled.jput.trend.PerformanceTrend

/**
 * TODO
 * - delay between executions
 *
 * Mark the test as a performance test, allowing to assert test execution time and analyze execution time trends
 *
 * @author Vlad on 20-May-17.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class PerformanceTest(

        /**
         * Count of warm up test executions
         */
        val warmUp: Int = 0,

        /**
         * Count of test execution repeats
         */
        val repeats: Int = 1,

        /**
         * Upper limit for test execution time in milliseconds
         */
        val maxTimeLimit: Long = 0L,

        /**
         * Upper limit for average execution time when using **repeats > 1**, in milliseconds
         */
        val averageTimeLimit: Long = 0L,

        val percentiles: LongArray = [],

        /**
         * Performance trend analyzing
         */
        val trends: Array<PerformanceTrend> = []

)
