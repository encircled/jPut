package cz.encircled.jput.model


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
        var executionResult: MutableList<Long> = mutableListOf()
)

