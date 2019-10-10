package cz.encircled.jput.model

import java.util.*

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
        val trendConfiguration: TrendTestConfiguration? = null,

        var percentiles: Map<Long, Double> = HashMap(1)

) {

    fun valid(): PerfTestConfiguration {
        check(warmUp >= 0L) { "WarmUp count must be > 0" }
        check(repeats >= 1L) { "Repeats count must be > 1" }
        check(trendConfiguration == null || trendConfiguration.sampleSize >= 1) {
            "Sample size must be > 0"
        }

        for (percentile in percentiles.keys) {
            check(percentile >= 1) { "Percentile value must be > 0" }
            check(percentile <= 100) { "Percentile value must be < 100" }
        }

        return this
    }

    override fun toString(): String {
        return "PerfTestConfiguration{" +
                "testId=$testId" +
                ", warmUp=$warmUp ms" +
                ", repeats=$repeats ms" +
                ", maxTimeLimit=$maxTimeLimit ms" +
                ", avgTimeLimit=$avgTimeLimit ms }"
    }

}
