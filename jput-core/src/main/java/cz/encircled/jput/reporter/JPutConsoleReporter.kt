package cz.encircled.jput.reporter

import cz.encircled.jput.model.PerfTestExecution
import org.slf4j.LoggerFactory

/**
 *
 * @author Vlad on 22-Sep-19.
 */
class JPutConsoleReporter : JPutBaseTextReporter() {

    private val log = LoggerFactory.getLogger(JPutConsoleReporter::class.java)

    override fun beforeClass(clazz: Class<*>) {
        log.info("Starting JPut performance tests for ${clazz.simpleName}")
    }

    override fun beforeTest(execution: PerfTestExecution) {
    }

    override fun afterTest(execution: PerfTestExecution) {
        log.info(buildTextReport(execution))
        log.info(buildErrorTextReport(execution))
    }

    override fun afterClass(clazz: Class<*>) {
    }
}