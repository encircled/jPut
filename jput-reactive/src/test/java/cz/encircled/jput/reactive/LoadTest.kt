package cz.encircled.jput.reactive

import cz.encircled.jput.runner.JPutJUnit4Runner
import cz.encircled.jput.unit.PerformanceTest
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.runner.RunWith
import org.springframework.web.reactive.function.client.WebClient
import java.util.concurrent.atomic.AtomicLong
import kotlin.test.Test


@RunWith(JPutJUnit4Runner::class)
class LoadTest {

    private val client: WebClient = WebClient
            .builder()
            .baseUrl("http://load.pif.test.eit.zone/api/customer")
            .build()

    companion object {

        @JvmStatic
        val counter = AtomicLong()

        var start: Long = 0L

        @JvmStatic
        @BeforeClass
        fun before() {
            start = System.nanoTime()
        }

        @JvmStatic
        @AfterClass
        fun after() {
            val end = System.nanoTime()

            val elapsedMs = (end - start) / 1000000.0

            val elapsedS = elapsedMs / 1000.0

            println("Responses count: $counter")
            println("Elapsed time: $elapsedMs ms, $elapsedS sec")
            println("Throughput: ${counter.get() / elapsedS}/sec")
        }

    }

    @Test
    @PerformanceTest(
            repeats = 10000,
            parallel = 10000,
            isReactive = true
    )
    @Ignore
    fun test() {
        client.get()
                .retrieve()
                .bodyToMono(Long::class.java)
                .map {
                    counter.incrementAndGet()
                }
                .jputTest()
    }

}