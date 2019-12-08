package cz.encircled.jput.annotation

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
         * TODO
         */
        val runTime: String = "",

        /**
         * Delay between test repeats, in ms
         */
        val delay: Long = 0,

        /**
         * Upper limit for test execution time in milliseconds
         */
        val maxTimeLimit: Long = 0,

        /**
         * Upper limit for average execution time when using **repeats > 1**, in milliseconds
         */
        val averageTimeLimit: Long = 0,

        /**
         * Upper limits for test execution time in milliseconds within defined percentiles. Defining multiple percentiles allows
         * to have multiple validation constraints, for instance [200ms for 75%] and [500ms for 95%]
         */
        val percentiles: Array<Percentile> = [],

        /**
         * Tests will run in parallel if threads count is greater than 1
         */
        val parallel: Int = 1,

        /**
         * Ramp-up in milliseconds. If parallel is 100, and the ramp-up period is 100000 (100 seconds),
         * then JPut will take 100 seconds to get all 100 threads running, i.e. 1 second delay after each new thread
         */
        val rampUp: Long = 0,

        val isReactive: Boolean = false,

        /**
         * Unit test will be marked as failed, if catched exceptions count is greater than this parameter
         */
        val maxAllowedExceptionsCount: Long = Long.MAX_VALUE,

        /**
         * If true, jput will catch runtime exceptions and save errored repeats with result code "500" and corresponding error message.
         */
        val continueOnException: Boolean = true,

        /**
         * Performance trend analyzing
         */
        val trends: Array<PerformanceTrend> = []

)
