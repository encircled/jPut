package cz.encircled.jput.reactive

import cz.encircled.jput.model.ExecutionRun
import cz.encircled.jput.model.ExecutionRunResultDetails
import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.runner.ThreadBasedTestExecutor
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.scheduler.Schedulers
import java.util.concurrent.CountDownLatch

/**
 * Mark this [Mono] as a reactive performance test body for JPut
 */
fun Mono<*>.jputTest() = JPutReactive.reactiveTestBody(this)

/**
 * @author Vlad on 21-Sep-19.
 */
// TODO re-design a bit, should not extend ThreadBased
class ReactiveTestExecutor : ThreadBasedTestExecutor() {

    // TODO delay
    override fun performExecution(execution: PerfTestExecution, statement: () -> Unit) {
        if (!execution.conf.isReactive) return super.performExecution(execution, statement)

        // Invoke test statement which should just create a Mono/Flux and store it in params
        statement.invoke()
        val body = execution.executionParams["__executor"] as Mono<*>?
                ?: throw IllegalStateException("Reactive function is not set for execution $execution, please use JPutReactive#reactiveTestBody")

        // TODO wrong partitioning
        (0 until execution.conf.warmUp).chunked(execution.conf.parallelCount).forEach { chunk ->
            val countDown = CountDownLatch(chunk.size)

            chunk.toFlux()
                    .flatMap { body }
                    .doOnNext { countDown.countDown() }
                    .subscribe()

            countDown.await()
        }

        val countDown = CountDownLatch(execution.conf.parallelCount)

        // TODO No ramp up
        (0 until execution.conf.repeats).toFlux()
                .parallel(execution.conf.parallelCount)
                .runOn(Schedulers.parallel())
                .flatMap {
                    // TODO use execution.startNextExecution?
                    val repeat = ExecutionRun(execution)
                    execution.executionResult[it.toLong()] = repeat

                    body.map { b ->
                        Pair(b, repeat)
                    }.onErrorContinue { t, _ ->
                        repeat.measureElapsed()
                        repeat.resultDetails = ExecutionRunResultDetails(error = t)
                    }
                }.doOnNext {
                    it.second.measureElapsed()
                }.doOnTerminate {
                    countDown.countDown()
                }
                .subscribe()

        countDown.await()

    }

}