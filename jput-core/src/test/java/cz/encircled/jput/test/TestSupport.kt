package cz.encircled.jput.test

import cz.encircled.jput.context.context
import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.model.PerfTestExecution

/**
 * @author Vlad on 27-May-17.
 */
object TestSupport {

    fun getTestExecution(config: PerfTestConfiguration, vararg times: Long): PerfTestExecution {
        val run = PerfTestExecution(config, mapOf("id" to context.executionId))
        run.executionResult = times.toMutableList()
        return run
    }

}
