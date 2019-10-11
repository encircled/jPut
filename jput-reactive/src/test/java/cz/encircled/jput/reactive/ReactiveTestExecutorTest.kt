package cz.encircled.jput.reactive

import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.runner.JPutJUnit4Runner
import org.junit.runner.RunWith
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.io.IOException
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

        // Should just pass
        executor.executeTest(conf) { "test" }
    }

    /**
     * Test that repeats are split into chunks defined by parallel parameter
     */
    @Test
    fun testReactiveExecutorCorrectChunks() {
        System.setProperty("reactor.schedulers.defaultPoolSize", "2")
        val executor = ReactiveTestExecutor()

        val conf = PerfTestConfiguration("ReactiveTestExecutorTest#testReactiveExecutorCorrectChunks", repeats = 4, parallelCount = 2, isReactive = true)
        val executeTest = executor.executeTest(conf, ::reactiveBodyWithDelay)

        // Assert chunk in paralleled
        assertTrue(getDif("1 start", "2 start") < delay / 2)
        assertTrue(getDif("1 end", "2 end") < delay / 2)

        // Assert delay between chunks
        assertTrue(getDif("1 start", "3 start") >= delay)

        // Assert second chunk in paralleled
        assertTrue(getDif("3 start", "4 start") < delay / 2)
        assertTrue(getDif("3 end", "4 end") < delay / 2)

        // Assert that executions are actually run in parallel and haven't wait for others
        assertTrue(executeTest.executionResult.values.all { it.elapsedTime < delay * 2 })
        assertTrue(executeTest.executionResult.values.all { it.elapsedTime >= delay - 2 })
    }

    @Test
    fun testReactiveWarmUpExecuted() {
        val counter = AtomicInteger(0)
        val executor = ReactiveTestExecutor()

        val conf = PerfTestConfiguration("ReactiveTestExecutorTest#reactiveBodyWithDelay", warmUp = 4, repeats = 1, parallelCount = 3, isReactive = true)
        executor.executeTest(conf) {
            1.toMono().map {
                counter.getAndIncrement()
            }
        }

        assertEquals(5, counter.get())
    }

    @Test
    @Ignore // FIXME
    fun testRampUp() {
        val conf = PerfTestConfiguration("ReactiveTestExecutorTest#testRampUp", repeats = 5, rampUp = 1000, parallelCount = 5, isReactive = true)

        ReactiveTestExecutor().executeTest(conf, ::reactiveBodyWithDelay)

        println()
        assertTrue(getDif("1 start", "2 start") > 240, "Actual: ${getDif("1 start", "2 start")}")
//        assertEquals(5, startTimes.size)
//
//        listOf(
//                Pair(0, 1000),
//                Pair(1, 750),
//                Pair(2, 500),
//                Pair(3, 250)
//        ).forEach {
//            assertTrue(startTimes[4] - startTimes[it.first] >= it.second - 2, "${startTimes[4] - startTimes[it.first]} for $it")
//        }
//
//        assertTrue(startTimes[4] - startTimes[0] < 1100)
    }

    private fun getDif(left: String, right: String) =
            (invocationTimes[left]!! - invocationTimes[right]!!).absoluteValue

    private fun reactiveError() =
            Mono.just("1").map {
                throw IOException("Test exception!")
            }

    private fun reactiveBodyWithDelay() =
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