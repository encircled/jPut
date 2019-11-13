package cz.encircled.jput.reactive

import cz.encircled.jput.JPut
import cz.encircled.jput.model.ExecutionRun
import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.model.RunResult
import cz.encircled.jput.runner.ThreadBasedTestExecutor
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.CountDownLatch

/**
 * @author Vlad on 21-Sep-19.
 */
// TODO re-design a bit, should not extend ThreadBased
class ReactiveTestExecutor : ThreadBasedTestExecutor() {

    private val log = LoggerFactory.getLogger(ReactiveTestExecutor::class.java)

    // TODO if reactive + has jPut arg - throw an exc
    // TODO delay
    override fun performExecution(execution: PerfTestExecution, statement: (JPut?) -> Any?) {
        if (!execution.conf.isReactive) return super.performExecution(execution, statement)

        // Invoke test statement which should just return a Mono
        val body = statement.invoke(null)
        check(body is Mono<*>) { "Reactive test must return Mono<*> object, without subscribing/blocking." }

        // TODO wrong partitioning
        val wuCountDown = CountDownLatch(execution.conf.warmUp)

        (0 until execution.conf.warmUp).toFlux()
                .flatMap { body as Mono<*> }
                .onErrorContinue { t, _ ->
                    log.warn("Error during warm up", t)
                }
                .doOnNext { wuCountDown.countDown() }
                .subscribe()

        wuCountDown.await()

        val rampUp = if (execution.conf.rampUp > 0) execution.conf.rampUp / (execution.conf.parallelCount - 1) else 0L
        val countDown = CountDownLatch(execution.conf.parallelCount)

        // TODO No ramp up delayElement(Duration.ofMillis(rampUp * index))
        (0 until execution.conf.repeats).toFlux()
                .parallel(execution.conf.parallelCount)
                .runOn(Schedulers.parallel())
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