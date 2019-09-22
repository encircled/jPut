package cz.encircled.jput.model

import cz.encircled.jput.context.context
import cz.encircled.jput.unit.PerformanceTest
import java.lang.reflect.Method
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

        val isReactive: Boolean = false,

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

    companion object {

        fun defaultTestId(method: Method) : String = "${method.declaringClass.simpleName}#${method.name}"

        fun fromAnnotation(conf: PerformanceTest, method: Method): PerfTestConfiguration {
            val trendConfig =
                    if (conf.trends.isNotEmpty()) TrendTestConfiguration.fromAnnotation(conf.trends[0])
                    else null

            val testId = if (conf.testId.isBlank()) defaultTestId(method)
            else {
                context.customTestIds[defaultTestId(method)] = conf.testId
                conf.testId
            }

            val methodConfiguration = PerfTestConfiguration(testId, conf.warmUp, conf.repeats, conf.delay,
                    conf.maxTimeLimit, conf.averageTimeLimit, conf.threads, conf.isReactive, trendConfig)

            /*TODO val percentiles = conf.percentiles
            check(percentiles.size % 2 == 0) { "Percentiles parameter count must be even" }
            for (i in 0 until percentiles.size - 1) {
                methodConfiguration.percentiles.put(percentiles[i], percentiles[i + 1]);
            } */

            return methodConfiguration.valid()
        }
    }

}
