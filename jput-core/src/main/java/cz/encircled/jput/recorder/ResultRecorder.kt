package cz.encircled.jput.recorder

import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.getProperty
import cz.encircled.jput.model.MethodTrendConfiguration
import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.trend.SelectionStrategy
import org.slf4j.LoggerFactory
import java.util.*

/**
 * @author Vlad on 21-May-17.
 */
interface ResultRecorder {

    fun getSample(execution: PerfTestExecution, config: MethodTrendConfiguration): List<Long>

    /**
     * Select sub list from samples according to the given [strategy]
     */
    fun <T> subList(input: List<T>, size: Int, strategy: SelectionStrategy): List<T> {
        return if (input.size > size) {
            when (strategy) {
                SelectionStrategy.USE_FIRST -> input.subList(0, size)
                SelectionStrategy.USE_LATEST -> input.subList(input.size - size, input.size)
                else -> throw UnsupportedOperationException()
            }
        } else {
            input
        }
    }

    /**
     * Get user-defined environment parameters, which should be persisted with test results
     */
    fun getUserDefinedEnvParams(): Map<String, String> {
        val value = getProperty(JPutContext.PROP_ENV_PARAMS, "")
        return value.split(",")
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