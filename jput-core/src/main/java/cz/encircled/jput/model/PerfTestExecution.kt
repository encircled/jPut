package cz.encircled.jput.model

import cz.encircled.jput.percentile
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.roundToLong


data class ExecutionRunResultDetails @JvmOverloads constructor(

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

        var resultDetails: ExecutionRunResultDetails = ExecutionRunResultDetails(200)) {

    val startTime: Long
        get() = execution.startTime + relativeStartTime

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
     * Holds start time of current execution
     */
    private var currentRunNum: ThreadLocal<Long> = ThreadLocal.withInitial { 1L }

    /**
     * Thread-safe counter
     */
    private val runCounter = AtomicLong()

    val violationsErrorMessage: List<String>
        get() = violations.map {
            it.messageProducer.invoke(this)
        }

    val executionAvg: Long by lazy {
        try {
            getElapsedTimes().average().roundToLong()
        } catch (e: Exception) {
            throw e
        }
    }

    val executionMax: Long by lazy { getElapsedTimes().max()!! }

    fun executionPercentile(rank: Double): Long =
            getElapsedTimes().percentile(rank).max()!!

    fun getElapsedTimes() = executionResult.values.map { it.elapsedTime }

    /**
     * Starts new execution, returns start time (nanoseconds)
     */
    fun startNextExecution(): ExecutionRun {
        val actual = runCounter.getAndIncrement()
        val repeat = ExecutionRun(this)
        executionResult[actual] = repeat
        currentRunNum.set(actual)
        return repeat
    }

    fun resetCurrentExecutionStartTime() {
        getCurrentRun().relativeStartTime = System.nanoTime() - startTime
    }

    fun getCurrentRun() = executionResult[currentRunNum.get()]!!

    fun finishExecution() {
        val repeat = executionResult[currentRunNum.get()]!!
        repeat.elapsedTime = (System.nanoTime() - repeat.startTime) / 1000000L
    }

}

