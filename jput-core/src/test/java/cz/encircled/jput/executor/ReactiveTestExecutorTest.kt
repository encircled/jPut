package cz.encircled.jput.executor

import cz.encircled.jput.JPut
import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.runner.ReactiveTestExecutor
import cz.encircled.jput.runner.junit.JPutJUnit4Runner
import org.junit.runner.RunWith
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.*


/**
 * @author Vlad on 21-Sep-19.
 */
@RunWith(JPutJUnit4Runner::class)
class ReactiveTestExecutorTest : BaseGenericExecutorTest() {

    private val delay = 100L

    private val startTime = System.currentTimeMillis()

    // TODO default exception handling + set result. Maybe configurable on suit level
    @Test
    fun testRuntimeExceptionRegistered() {
        val executor = ReactiveTestExecutor()

        val conf = PerfTestConfiguration("ReactiveTestExecutorTest#testRuntimeExceptionRegistered", isReactive = true)
        val executeTest = executor.executeTest(conf, ::stepWithError)
        assertEquals("Test exception!", executeTest.executionResult[0]!!.resultDetails.errorMessage)
    }

    @Test
    fun testWrongTestReturnType() {
        val executor = ReactiveTestExecutor()

        val conf = PerfTestConfiguration("ReactiveTestExecutorTest#testWrongTestReturnType", isReactive = true)
        assertFailsWith(IllegalStateException::class, "Reactive test must return Mono<*> object, without subscribing/blocking.") {
            executor.executeTest(conf) { "test" }
        }
    }

    @Test
    fun testNonReactiveTest() {
        val executor = ReactiveTestExecutor()

        val conf = PerfTestConfiguration("ReactiveTestExecutorTest#testWrongTestReturnType", isReactive = false)

        // Should fail
        assertFails {
            executor.executeTest(conf) { "test" }
        }
    }

    /**
     * Test that repeats are split into chunks defined by parallel parameter
     */
    @Test
    @Ignore // TODO
    fun testReactiveExecutorCorrectChunks() {
        // TODO this probably affects all the other tests...
        System.setProperty("reactor.schedulers.defaultPoolSize", "2")

        val delayWithError = delay - 4
        val executor = ReactiveTestExecutor()

        val conf = PerfTestConfiguration("ReactiveTestExecutorTest#testReactiveExecutorCorrectChunks",
                repeats = 4,
                parallelCount = 2,
                isReactive = true,
                delay = delay
        )
        val executeTest = executor.executeTest(conf, ::reactiveBodyWithDelay)

        println(invocationTimes.mapValues { it.value - startTime }.toSortedMap())

        // Assert chunk in paralleled
        assertTrue(invocationDif("0 start", "1 start") < delay / 2)
        assertTrue(invocationDif("0 end", "1 end") < delay / 2)

        // Assert delay between chunks
        assertTrue(invocationDif("0 start", "1 start") >= delayWithError)

        // Assert second chunk in paralleled
        assertTrue(invocationDif("2 start", "3 start") < delay / 2, invocationDif("2 start", "3 start").toString())
        assertTrue(invocationDif("2 end", "3 end") < delay / 2)

        // Assert that executions are actually run in parallel and haven't wait for others
        assertTrue(executeTest.executionResult.values.all { it.elapsedTime < delayWithError * 2 })
        assertTrue(executeTest.executionResult.values.all { it.elapsedTime >= delayWithError })
    }

    @Test
    fun testReactiveWarmUpExecuted() {
        val counter = AtomicInteger(0)
        val executor = ReactiveTestExecutor()

        val conf = PerfTestConfiguration("ReactiveTestExecutorTest#reactiveBodyWithDelay", warmUp = 14, repeats = 1, parallelCount = 3, isReactive = true)
        executor.executeTest(conf) {
            1.toMono().map {
                counter.getAndIncrement()
            }
        }

        assertEquals(15, counter.get())
    }

    override fun getExecutor() = ReactiveTestExecutor()

    override fun stepWithSimpleCounter(jput: JPut?) =
            "".toMono().map {
                invocationTimes["${counter.getAndIncrement()} start"] = System.currentTimeMillis()
            }

    override fun stepWithDelay(jput: JPut?) =
            "".toMono().map {
                invocationTimes["${counter.getAndIncrement()} start"] = System.currentTimeMillis()
            }.delayElement(Duration.ofMillis(500))

    override fun stepWithError(jput: JPut?) =
            "".toMono().map {
                invocationTimes["${counter.getAndIncrement()} start"] = System.currentTimeMillis()
                throw RuntimeException("Test exception!")
            }


    private fun reactiveBodyWithDelay(jPut: JPut?): Mono<*> {
        val start = System.currentTimeMillis()
        return Mono.just("")
                .map {
                    val index = counter.getAndIncrement()
                    invocationTimes["$index start"] = System.currentTimeMillis() - start
                    println("Run $index on ${Thread.currentThread().name}")
                    Thread.sleep(520)
                    index
                }
                .map {
                    invocationTimes["$it end"] = System.currentTimeMillis() - start
                }
    }

}