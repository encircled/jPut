package cz.encircled.jput.model

/**
 * Performance test configuration of a single junit test method
 *
 * @author Vlad on 20-May-17.
 */
data class PerfTestConfiguration(

        /**
         * Unique test id, used to distinguish different tests. Default to `testClassName#testMethodName`
         */
        val testId: String,

        /**
         * Count of warm up test executions
         */
        val warmUp: Int = 0,

        /**
         * Test execution repeats count
         */
        val repeats: Int = 1,

        /**
         * TODO
         */
        val runTime: Long = 0L,

        /**
         * Delay between test repeats, in ms
         */
        val delay: Long = 0L,

        /**
         * Upper limit for test execution time in milliseconds
         */
        val maxTimeLimit: Long = 0L,

        /**
         * Upper limit for average execution time when using **repeats > 1**, in milliseconds
         */
        val avgTimeLimit: Long = 0L,

        /**
         * Upper limits for test execution time in milliseconds within defined percentiles. Defining multiple percentiles allows
         * to have multiple validation constraints, for instance [200ms for 75%] and [500ms for 95%].
         *
         * Map is rank to maxTime
         */
        val percentiles: Map<Double, Long> = emptyMap(),

        /**
         * Count of maximum parallel executions (e.g. java threads in case of base executor or coroutines/reactive executions)
         */
        val parallelCount: Int = 1,

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
         * TODO implement: reactive
         *
         * If true, jput will catch runtime exceptions and save errored repeats with result code "500" and corresponding error message.
         */
        val continueOnException: Boolean = true,

        /**
         * Performance trend analyzing
         */
        val trendConfiguration: TrendTestConfiguration? = null

) {

    fun valid(): PerfTestConfiguration {
        check(warmUp >= 0L) { "WarmUp count must be > 0" }
        check(repeats >= 1L) { "Repeats count must be > 1" }
        check(trendConfiguration == null || trendConfiguration.sampleSize >= 1) {
            "Sample size must be > 0"
        }
        percentiles.forEach {
            check(it.key in 0.0..1.0) { "Percentiles must be within 0..100" }
            check(it.value > 0L) { "Time limit for percentiles must be > 0" }
        }

        return this
    }

    override fun toString(): String {
        return "PerfTestConfiguration{" +
                "testId=$testId" +
                ", warmUp=$warmUp ms" +
                ", repeats=$repeats ms" +
                ", maxTimeLimit=$maxTimeLimit ms" +
                ", avgTimeLimit=$avgTimeLimit ms }" +
                ", percentiles=${percentiles.entries.joinToString()}"
    }

}
