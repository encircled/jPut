package cz.encircled.jput.model

import cz.encircled.jput.percentile
import org.joda.time.DateTime
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToLong

data class ExecutionRepeat(

        /**
         * Parent execution
         */
        val execution: PerfTestExecution? = null,

        /**
         * Start time in nanoseconds
         */
        var startTime: Long,

        /**
         * Elapsed time in ms
         */
        var elapsedTime: Long = 0L,

        var startDate: DateTime = DateTime.now(),

        var resultCode: Int? = null,

        var error: Throwable? = null) {

    override fun toString(): String = "startTime: $startTime, elapsed: $elapsedTime"

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
         * Repeat number to its execution
         */
        val executionResult: MutableMap<Long, ExecutionRepeat> = ConcurrentHashMap(32),

        /**
         * Validation result is set after executions
         */
        val violations: MutableList<PerfConstraintViolation> = mutableListOf()

) {

    /**
     * Holds start time of current execution
     */

    private var currentRepeatNum: ThreadLocal<Long> = ThreadLocal.withInitial { 1L }

    val executionAvg: Long by lazy {
        getElapsedTimes().average().roundToLong()
    }

    val executionMax: Long by lazy { getElapsedTimes().max()!! }

    fun executionPercentile(rank: Double): Long =
            getElapsedTimes().percentile(rank).max()!!

    fun getElapsedTimes() = executionResult.values.map { it.elapsedTime }

    /**
     * Starts new execution, returns start time (nanoseconds)
     */
    fun startNextExecution(): ExecutionRepeat {
        val actual = currentRepeatNum.get()
        val repeat = ExecutionRepeat(this, System.nanoTime())
        executionResult[actual] = repeat
        currentRepeatNum.set(actual.plus(1))
        return repeat
    }

    fun resetCurrentExecutionStartTime() {
        executionResult[currentRepeatNum.get() - 1]!!.startTime = System.nanoTime()
    }

    fun finishExecution() {
        val repeat = executionResult[currentRepeatNum.get() - 1]!!
        repeat.elapsedTime = (System.nanoTime() - repeat.startTime) / 1000000L
    }

}

