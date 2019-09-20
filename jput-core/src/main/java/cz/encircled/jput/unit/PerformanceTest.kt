package cz.encircled.jput.unit

import cz.encircled.jput.trend.PerformanceTrend

/**
 * Mark the test as a JPut performance test, allowing to assert test execution time and analyze execution time trends
 *
 * @author Vlad on 20-May-17.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class PerformanceTest(

        /**
         * Unique test id, used to distinguish different tests. Default to `testClassName#testMethodName`
         */
        val testId: String = "",

        /**
         * Count of warm up test executions
         */
        val warmUp: Int = 0,

        /**
         * Count of test execution repeats
         */
        val repeats: Int = 1,

        /**
         * Delay between test repeats, in ms
         */
        val delay: Long = 50L,

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
         * Tests will run in parallel if threads count is greater than 1
         */
        val threads: Int = 1,

        /**
         * Performance trend analyzing
         */
        val trends: Array<PerformanceTrend> = []

)
