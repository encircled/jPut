package cz.encircled.jput.model

import cz.encircled.jput.Statistics


/**
 * @author Vlad on 20-May-17.
 */
data class PerfTestExecution(

        /**
         * Parameters related to the global execution
         */
        var executionParams: Map<String, Any>,

        /**
         * Identifier of the test
         */
        var testId: String? = null,

        /**
         * List of result execution times in ms
         */
        var executionResult: MutableList<Long> = mutableListOf(),

        /**
         * Sample execution times in ms, which is used for trend analysis
         */
        var sample: MutableList<Long> = mutableListOf()

) {

    val executionAvg: Long by lazy {
        Statistics.round(Statistics.getAverage(executionResult))
    }

    val sampleAvg: Long by lazy {
        Statistics.round(Statistics.getAverage(sample))
    }

    val executionMax: Long by lazy { executionResult.max()!! }

}

