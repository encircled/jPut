package cz.encircled.jput.reactive

import cz.encircled.jput.JPut
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

/**
 * @author Vlad on 21-Sep-19.
 */
object JPutReactive {

    private val log = LoggerFactory.getLogger(JPutReactive::class.java)

    fun reactiveTestBody(body: Mono<*>) {
        val (caller, execution) = JPut.getCurrentExecution()

        if (execution == null) {
            log.warn("[$caller] is not a JPut test, ignoring [markPerformanceTestStart]")
        } else {
            execution.executionParams["__executor"] = body
        }
    }

}