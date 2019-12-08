package cz.encircled.jput.executor

import cz.encircled.jput.JPut
import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.runner.ReactiveTestExecutor
import cz.encircled.jput.runner.junit.JPutJUnit4Runner
import org.junit.runner.RunWith
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.io.IOException
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.absoluteValue
import kotlin.test.*


/**
 * @author Vlad on 21-Sep-19.
 */
@RunWith(JPutJUnit4Runner::class)
class ReactiveTestExecutorTest {

    private var increment = AtomicInteger(1)

    private val delay = 100L

    private val invocationTimes = ConcurrentHashMap<String, Long>()

    @BeforeTest
    fun before() {
        increment.set(1)
    }

    // TODO default exception handling + set result. Maybe configurable on suit level
    @Test
    fun testRuntimeExceptionRegistered() {
        val executor = ReactiveTestExecutor()

        val conf = PerfTestConfiguration("ReactiveTestExecutorTest#testRuntimeExceptionRegistered", isReactive = true)
        val executeTest = executor.executeTest(conf) { reactiveError() }
        assertEquals("Test exception!", executeTest.executionResult[0]!!.resultDetails.errorMessage)
    }

    @Test
    fun testWrongTestReturnType() {
        val executor = ReactiveTestExecutor()

        val conf = PerfTestConfiguration("ReactiveTestExecutorTest#testWrongTestReturnType", isReactive = true)
        try {
            executor.executeTest(conf) { "test" }
        } catch (e: Exception) {
            assertEquals("Reactive test must return Mono<*> object, without subscribing/blocking.", e.message)
            return
        }

        fail("Exception expected")
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
    fun testReactiveExecutorCorrectChunks() {
        // TODO this probably affects all the other tests...
        System.setProperty("reactor.schedulers.defaultPoolSize", "2")

        val delayWithError = delay - 2
        val executor = ReactiveTestExecutor()

        val conf = PerfTestConfiguration("ReactiveTestExecutorTest#testReactiveExecutorCorrectChunks", repeats = 4, parallelCount = 2, isReactive = true)
        val executeTest = executor.executeTest(conf, ::reactiveBodyWithDelay)

        // Assert chunk in paralleled
        assertTrue(getDif("1 start", "2 start") < delay / 2)
        assertTrue(getDif("1 end", "2 end") < delay / 2)

        // Assert delay between chunks
        assertTrue(getDif("1 start", "3 start") >= delayWithError)

        // Assert second chunk in paralleled
        assertTrue(getDif("3 start", "4 start") < delay / 2, getDif("3 start", "4 start").toString())
        assertTrue(getDif("3 end", "4 end") < delay / 2)

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

    // FIXME
    @Test
    fun testRampUp() {
        val conf = PerfTestConfiguration("ReactiveTestExecutorTest#testRampUp", repeats = 5, rampUp = 1000, parallelCount = 5, isReactive = true)

        ReactiveTestExecutor().executeTest(conf, ::reactiveBodyWithDelay)

        println()
        assertTrue(getDif("1 start", "2 start") > 240, "Actual: ${getDif("1 start", "2 start")}")

        getDif("1 start", "2 start")

        listOf(
                Pair(1, 950),
                Pair(2, 700),
                Pair(3, 450),
                Pair(4, 200)
        ).forEach {
            assertTrue(getDif("${it.first} start", "5 start") > it.second)
        }

        assertTrue(getDif("1 start", "5 start") < 1100)
    }

    @Test
    fun test() {
        URL("http://seznam.cz").openConnection()
    }

    private fun getDif(left: String, right: String) =
            (invocationTimes[left]!! - invocationTimes[right]!!).absoluteValue

    private fun reactiveError() =
            Mono.just("1").map {
                throw IOException("Test exception!")
            }

    private fun reactiveBodyWithDelay(jPut: JPut?) =
            Mono.just("")
                    .map {
                        val index = increment.getAndIncrement()
                        invocationTimes["$index start"] = System.currentTimeMillis()
                        Thread.sleep(delay)
                        index
                    }
                    .map {
                        invocationTimes["$it end"] = System.currentTimeMillis()
                    }


}