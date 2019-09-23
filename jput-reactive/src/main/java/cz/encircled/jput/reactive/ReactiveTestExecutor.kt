package cz.encircled.jput.reactive

import cz.encircled.jput.model.ExecutionRepeat
import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.runner.ThreadBasedTestExecutor
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import java.util.concurrent.CountDownLatch

/**
 * Mark this [Mono] as a reactive performance test body for JPut
 */
fun Mono<*>.jputTest() = JPutReactive.reactiveTestBody(this)

/**
 * @author Vlad on 21-Sep-19.
 */
class ReactiveTestExecutor : ThreadBasedTestExecutor() {

    // TODO delay
    override fun performExecution(execution: PerfTestExecution, statement: () -> Unit) {
        if (!execution.conf.isReactive) return super.performExecution(execution, statement)

        // Invoke test statement which should just create a Mono/Flux and store it in params
        statement.invoke()
        val body = execution.executionParams["__executor"] as Mono<*>?
                ?: throw IllegalStateException("Reactive function is not set, please use JPutReactive#reactiveTestBody")

        (1..execution.conf.warmUp).chunked(execution.conf.parallelCount).forEach { chunk ->
            val countDown = CountDownLatch(chunk.size)

            chunk.toFlux()
                    .flatMap { body }
                    .doOnNext { countDown.countDown() }
                    .subscribe()

            countDown.await()
        }

        (1..execution.conf.repeats).chunked(execution.conf.parallelCount).forEach { chunk ->
            val countDown = CountDownLatch(chunk.size)

            chunk.toFlux()
                    .flatMap {
                        val repeat = ExecutionRepeat(execution, System.nanoTime())
                        execution.executionResult[it.toLong()] = repeat

                        body.map { b ->
                            Pair(b, repeat)
                        }
                    }.doOnNext {
                        it.second.elapsedTime = (System.nanoTime() - it.second.startTime) / 1000000L
                        countDown.countDown()
                    }.subscribe()

            countDown.await()
        }

    }

}