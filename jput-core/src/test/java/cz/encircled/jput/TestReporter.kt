package cz.encircled.jput

import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.reporter.JPutReporter

class TestReporter : JPutReporter {

    val invocations = mutableListOf<Pair<String, Any?>>()

    val executions = mutableListOf<PerfTestExecution>()

    fun getExecution(id: String) = executions.find {
        it.conf.testId.endsWith(id)
    } ?: throw RuntimeException("Test ")

    override fun beforeClass(clazz: Class<*>) {
        invocations.add("beforeClass" to clazz)
    }

    override fun beforeTest(execution: PerfTestExecution) {
        invocations.add("beforeTest" to execution.conf.testId)
    }

    override fun afterTest(execution: PerfTestExecution) {
        invocations.add("afterTest" to execution.conf.testId)
        executions.add(execution)
    }

    override fun afterClass(clazz: Class<*>) {
        invocations.add("afterClass" to clazz)
    }

}