package cz.encircled.jput.model

import cz.encircled.jput.percentile
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.roundToLong


data class RunResult @JvmOverloads constructor(

        val resultCode: Int? = null,

        val error: Throwable? = null,

        var errorMessage: String? = error?.message

)

/**
 * Each test (i.e. [PerfTestExecution]) may have multiple repeats, this class represent a single repeat/run
 */
data class ExecutionRun(

        /**
         * Parent execution
         */
        val execution: PerfTestExecution,

        /**
         * Start time in nanoseconds relative to execution start time (i.e. this particular repeat start minus parent execution start)
         */
        var relativeStartTime: Long = System.nanoTime() - execution.startTime,

        /**
         * Elapsed time in ms
         */
        var elapsedTime: Long = 0L,

        var resultDetails: RunResult = RunResult(200)) {

    val startTime: Long
        get() = execution.startTime + relativeStartTime

    val isError: Boolean
        get() = resultDetails.error != null || resultDetails.errorMessage != null

    /**
     * Set elapsed time as difference before current time and start time
     */
    fun measureElapsed() {
        elapsedTime = (System.nanoTime() - startTime) / 1000000L
    }

    override fun toString(): String = "startTime: ${startTime}, elapsed: $elapsedTime"

}

/**
 * Represents the execution state of a particular performance test
 *
 * @author Vlad on 20-May-17.
 */
data class PerfTestExecution(

        /**
         * Configuration used for this execution
         */
        val conf: PerfTestConfiguration,

        /**
         * Parameters related to the global execution
         */
        val executionParams: MutableMap<String, Any>,

        /**
         * Start time of this execution in nanoseconds
         */
        val startTime: Long,

        /**
         * Repeat number to its execution
         */
        val executionResult: MutableMap<Long, ExecutionRun> = ConcurrentHashMap(32),

        /**
         * Validation result is set after executions
         */
        val violations: MutableList<PerfConstraintViolation> = mutableListOf()

) {

    /**
     * Thread-safe counter
     */
    private val runCounter = AtomicLong()

    /**
     * Build error messages from [violations]
     */
    val violationsErrorMessage: List<String>
        get() = violations.map {
            it.messageProducer.invoke(this)
        }

    val executionAvg: Long by lazy {
        if (successResults().isEmpty()) 0
        else getElapsedTimes().average().roundToLong()
    }

    val executionMax: Long by lazy {
        if (successResults().isEmpty()) 0
        else getElapsedTimes().max()!!
    }

    /**
     * Get max execution time for given percentile [rank]
     */
    fun executionPercentile(rank: Double): Long =
            if (successResults().isEmpty()) 0
            else getElapsedTimes().percentile(rank).max()!!

    /**
     * Only success [executionResult]
     */
    fun successResults() = executionResult.values.filter { !it.isError }

    fun errorResults() = executionResult.values.filter { it.isError }

    /**
     * Count of repeats which finished with an exception
     */
    fun exceptionsCount() = errorResults().size

    /**
     * List of elapsed times of all repeats
     */
    fun getElapsedTimes() = successResults().map { it.elapsedTime }

    /**
     * Starts new execution, returns start time (nanoseconds)
     */
    fun startNextExecution(): ExecutionRun {
        val actual = runCounter.getAndIncrement()
        val repeat = ExecutionRun(this)
        executionResult[actual] = repeat
        return repeat
    }

}

