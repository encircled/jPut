package cz.encircled.jput.recorder

import cz.encircled.jput.model.PerfTestExecution
import org.slf4j.LoggerFactory
import java.util.*

/**
 * @author Vlad on 21-May-17.
 */
interface ResultRecorder {

    fun getSample(execution: PerfTestExecution, sampleSize: Int): List<Long>

    /**
     * Append the new execution result
     */
    fun appendTrendResult(execution: PerfTestExecution)

    /**
     * Flush appended data to the storage
     */
    fun flush()

    /**
     * Graceful destroy, called after test
     */
    fun destroy() {

    }

}

abstract class ThreadsafeResultRecorder : ResultRecorder {

    private val log = LoggerFactory.getLogger(ThreadsafeResultRecorder::class.java)

    private val stack = ArrayList<PerfTestExecution>()

    private val flushMutex = Any()

    override fun appendTrendResult(execution: PerfTestExecution) {
        synchronized(stack) {
            stack.add(execution)
            log.info(execution.toString())
        }
    }

    override fun flush() {
        synchronized(flushMutex) {
            var copy: List<PerfTestExecution>
            synchronized(stack) {
                copy = ArrayList(stack)
                stack.clear()
            }
            doFlush(copy)
        }
    }

    abstract fun doFlush(data: List<PerfTestExecution>)

}