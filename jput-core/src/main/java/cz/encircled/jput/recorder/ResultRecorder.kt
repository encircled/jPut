package cz.encircled.jput.recorder

import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.getCollectionProperty
import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.trend.SelectionStrategy
import org.slf4j.LoggerFactory

/**
 * @author Vlad on 21-May-17.
 */
interface ResultRecorder {

    /**
     * Fetch execution times sample for the given [execution]
     */
    fun getSample(execution: PerfTestExecution): List<Long>

    /**
     * Select sub list of given [size] from [sample] according to the given [strategy]
     */
    fun <T> subList(sample: List<T>, size: Int, strategy: SelectionStrategy): List<T> {
        return if (sample.size > size) {
            when (strategy) {
                SelectionStrategy.USE_FIRST -> sample.subList(0, size)
                SelectionStrategy.USE_LATEST -> sample.subList(sample.size - size, sample.size)
            }
        } else {
            sample
        }
    }

    /**
     * Get user-defined environment parameters, which should be persisted along with test results
     */
    fun getUserDefinedEnvParams(): Map<String, String> {
        return getCollectionProperty(JPutContext.PROP_ENV_PARAMS)
                .map { it.split(":") }
                .associateBy({ it[0] }, { it[1] })
    }

    /**
     * Append the new execution result
     */
    fun appendTrendResult(execution: PerfTestExecution)

    /**
     * Flush appended data to the storage
     */
    fun flush()

}

/**
 * Primitive thread-safe abstract impl of [ResultRecorder]. Adds synchronization on append and flush.
 */
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
        log.info("Do flush execution results")
        synchronized(flushMutex) {
            val copy = synchronized(stack) {
                val c = ArrayList(stack)
                stack.clear()
                c
            }
            doFlush(copy)
        }
    }

    abstract fun doFlush(data: List<PerfTestExecution>)

}