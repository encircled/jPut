package cz.encircled.jput.reactive

import cz.encircled.jput.JPut
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

/**
 * @author Vlad on 21-Sep-19.
 */
object JPutReactive {

    private val log = LoggerFactory.getLogger(JPutReactive::class.java)

    // TODO consider just adding test ID
    fun reactiveTestBody(body: Mono<*>) {
        val execution = JPut.getCurrentExecution()

        if (execution == null) {
            log.warn("Ignoring [reactiveTestBody] since it is called from non JPut test")
        } else {
            execution.executionParams["__executor"] = body
        }
    }

}