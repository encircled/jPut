package cz.encircled.jput.test

import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.reporter.JPutReporter

class TestReporter : JPutReporter {

    val invocations = mutableListOf<Pair<String, Any?>>()

    override fun beforeClass(clazz: Class<*>) {
        invocations.add("beforeClass" to clazz)
    }

    override fun beforeTest(execution: PerfTestExecution) {
        invocations.add("beforeTest" to execution.conf.testId)
    }

    override fun afterTest(execution: PerfTestExecution) {
        invocations.add("afterTest" to execution.conf.testId)
    }

    override fun afterClass(clazz: Class<*>) {
        invocations.add("afterClass" to clazz)
    }

}