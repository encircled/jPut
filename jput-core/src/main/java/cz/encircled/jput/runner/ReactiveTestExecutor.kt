package cz.encircled.jput.runner

import cz.encircled.jput.JPut
import cz.encircled.jput.model.ExecutionRun
import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.model.RunResult
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicLong

/**
 * @author Vlad on 21-Sep-19.
 */
class ReactiveTestExecutor : BaseTestExecutor() {

    // TODO if reactive + has jPut arg - throw an exc
    // TODO delay
    override fun performExecution(execution: PerfTestExecution, statement: (JPut?) -> Any?) {
        check(execution.conf.isReactive)

        // Invoke test statement which should just return a Mono
        val body = statement.invoke(null)
        check(body is Mono<*>) { "Reactive test must return Mono<*> object, without subscribing/blocking." }

        // TODO wrong partitioning
        val warmUpCountDown = CountDownLatch(execution.conf.warmUp)

        (0 until execution.conf.warmUp).toFlux()
                .flatMap { body as Mono<*> }
                .onErrorContinue { t, _ ->
                    log.warn("Error during warm up", t)
                }
                .doOnNext { warmUpCountDown.countDown() }
                .subscribe()

        warmUpCountDown.await()

        val rampUp = if (execution.conf.rampUp > 0) execution.conf.rampUp / (execution.conf.parallelCount - 1) else 0L
        val parallelIndex = AtomicLong()
        val countDown = CountDownLatch(execution.conf.parallelCount)

        // TODO No ramp up delayElement(Duration.ofMillis(rampUp * index))
        (0 until execution.conf.repeats).toFlux()
                .parallel(execution.conf.parallelCount)
                .runOn(Schedulers.parallel())
                .flatMap {
                    it.toMono().delayElement(Duration.ofMillis(rampUp * parallelIndex.getAndIncrement()))
                }
                .flatMap { index ->
                    // TODO use execution.startNextExecution?
                    val repeat = ExecutionRun(execution)
                    execution.executionResult[index.toLong()] = repeat

                    body.flatMap { b ->
                        repeat.measureElapsed()
                        Pair(b, repeat).toMono()
                    }.onErrorContinue { t, _ ->
                        // TODO add if (e is AssumptionViolatedException || !execution.conf.continueOnException) {
                        repeat.measureElapsed()
                        repeat.resultDetails = RunResult(error = t)
                    }
                }.doOnTerminate {
                    countDown.countDown()
                }
                .subscribe()

        countDown.await()

    }

}