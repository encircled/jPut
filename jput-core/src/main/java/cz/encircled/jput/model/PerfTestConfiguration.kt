package cz.encircled.jput.model

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
        val warmUp: Int,

        /**
         * Test execution repeats count
         */
        val repeats: Int,

        /**
         * Delay between test repeats, in ms
         */
        val delay: Long,

        /**
         * Upper limit for test execution time in milliseconds
         */
        val maxTimeLimit: Long,

        /**
         * Upper limit for average execution time when using **repeats > 1**, in milliseconds
         */
        val avgTimeLimit: Long,

        /**
         * Performance trend analyzing
         */
        val trendConfiguration: TrendTestConfiguration? = null,

        var percentiles: Map<Long, Double> = HashMap(1)

) {

    fun valid(): PerfTestConfiguration {
        if (warmUp < 0L) {
            throw IllegalStateException("WarmUp count must be > 0")
        }
        if (repeats < 1L) {
            throw IllegalStateException("Repeats count must be > 1")
        }
        if (trendConfiguration != null) {
            if (trendConfiguration.sampleSize < 1) {
                throw IllegalStateException("Sample size must be > 0")
            }
        }

        for (percentile in percentiles.keys) {
            if (percentile < 1) {
                throw IllegalStateException("Percentile value must be > 0")
            }
            if (percentile > 100) {
                throw IllegalStateException("Percentile value must be < 100")
            }
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

        fun fromAnnotation(conf: PerformanceTest, method: Method): PerfTestConfiguration {
            val trendConfig =
                    if (conf.trends.isNotEmpty()) TrendTestConfiguration.fromAnnotation(conf.trends[0])
                    else null

            val percentiles = conf.percentiles
            if (percentiles.size % 2 != 0) {
                throw IllegalStateException("Percentiles parameter count must be even")
            }

            val testId =
                    if (conf.testId.isEmpty()) "${method.declaringClass.simpleName}#${method.name}"
                    else conf.testId

            val methodConfiguration = PerfTestConfiguration(testId, conf.warmUp, conf.repeats, conf.delay,
                    conf.maxTimeLimit, conf.averageTimeLimit, trendConfig)

            for (i in 0 until percentiles.size - 1) {
                // methodConfiguration.percentiles.put(percentiles[i], percentiles[i + 1]); TODO
            }

            return methodConfiguration.valid()
        }
    }

}
