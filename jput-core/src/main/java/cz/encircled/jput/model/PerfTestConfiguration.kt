package cz.encircled.jput.model

import cz.encircled.jput.unit.PerformanceTest
import java.util.*

/**
 * Performance test configuration of a single junit test method
 *
 * @author Vlad on 20-May-17.
 */
data class PerfTestConfiguration(
        val warmUp: Int,

        /**
         * Test execution repeats count
         */
        val repeats: Int,

        // TODO use 100 percentile instead?
        val maxTimeLimit: Long,
        val avgTimeLimit: Long,

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
                "warmUp=" + warmUp + " ms" +
                ", repeats=" + repeats + " ms" +
                ", maxTimeLimit=" + maxTimeLimit + " ms" +
                ", avgTimeLimit=" + avgTimeLimit + " ms" +
                '}'.toString()
    }

    companion object {

        fun fromAnnotation(conf: PerformanceTest): PerfTestConfiguration {
            val trendConfig =
                    if (conf.trends.isNotEmpty()) TrendTestConfiguration.fromAnnotation(conf.trends[0])
                    else null

            val percentiles = conf.percentiles
            if (percentiles.size % 2 != 0) {
                throw IllegalStateException("Percentiles parameter count must be even")
            }

            val methodConfiguration = PerfTestConfiguration(conf.warmUp, conf.repeats, conf.maxTimeLimit,
                    conf.averageTimeLimit, trendConfig)

            for (i in 0 until percentiles.size - 1) {
                // methodConfiguration.percentiles.put(percentiles[i], percentiles[i + 1]); TODO
            }

            return methodConfiguration.valid()
        }
    }

}
