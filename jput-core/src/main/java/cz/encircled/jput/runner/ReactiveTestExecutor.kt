package cz.encircled.jput.runner

import cz.encircled.jput.JPut
import cz.encircled.jput.model.ExecutionRun
import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.model.RunResult
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicLong

/**
 * @author Vlad on 21-Sep-19.
 */
class ReactiveTestExecutor : BaseTestExecutor() {

    // TODO if reactive + has jPut arg - throw an exc
    // TODO delay
    override fun performExecution(execution: PerfTestExecution, statement: (JPut?) -> Any?) {
        // Invoke test statement which should just return a Mono
        val body = statement.invoke(null)
        check(body is Mono<*>) { "Reactive test must return Mono<*> object, without subscribing/blocking." }

        // TODO wrong partitioning
        val warmUpCountDown = CountDownLatch(1)

        (0 until execution.conf.warmUp).toFlux()
                .flatMap { body as Mono<*> }
                .onErrorContinue { t, _ ->
                    log.warn("Error during warm up", t)
                }
                .doOnTerminate { warmUpCountDown.countDown() }
                .subscribe()

        warmUpCountDown.await()

        val rampUp = rampUpPerThread(execution)
        val countDown = CountDownLatch(execution.conf.repeats)

        val runningCount = AtomicLong(0)
        var parallelLimit = if (rampUp == 0L) execution.conf.parallelCount else 1

        (0 until execution.conf.repeats).toFlux()
                .parallel(Schedulers.DEFAULT_POOL_SIZE, execution.conf.parallelCount)
                .runOn(Schedulers.parallel())
                .flatMap { index ->
                    println("Attempt: running: $runningCount, limit: $parallelLimit")
                    var lock = true

                    while (lock) {
                        val actual = runningCount.get()
                        if (actual < parallelLimit) {
                            if (runningCount.compareAndSet(actual, actual + 1)) {
                                lock = false
                            }
                        }
                    }

                    println("Acquired!: running: $runningCount, limit: $parallelLimit")
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
                    }.doOnTerminate {
                        countDown.countDown()
                        runningCount.decrementAndGet()
                    }
                }
                .subscribe()

        // RampUp impl - increase parallelLimit in time
        Thread {
            while (parallelLimit < execution.conf.parallelCount) {
                Thread.sleep(rampUp)
                parallelLimit++
            }
        }.start()

        countDown.await()
    }

    inline fun acquireLockForNextRun(runningCount: AtomicLong, parallelLimit: Array<Int>) {
        var lock = true

        while (lock) {
            val actual = runningCount.get()
            if (actual < parallelLimit[0]) {
                if (runningCount.compareAndSet(actual, actual + 1)) {
                    lock = false
                }
            }
        }
    }

}