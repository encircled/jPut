package cz.encircled.jput.reporter

import cz.encircled.jput.model.PerfTestExecution

/**
 * Allows to report the state of executed performance tests. Implementation class name must be set using property `jput.reporter.class`
 */
interface JPutReporter {

    fun beforeClass(clazz: Class<*>)

    fun beforeTest(execution: PerfTestExecution)

    fun afterTest(execution: PerfTestExecution)

    fun afterClass(clazz: Class<*>)

}