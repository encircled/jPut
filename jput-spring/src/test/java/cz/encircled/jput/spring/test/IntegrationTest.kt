package cz.encircled.jput.spring.test

import cz.encircled.jput.trend.PerformanceTrend
import cz.encircled.jput.trend.SelectionStrategy
import cz.encircled.jput.unit.PerformanceTest
import org.junit.Ignore
import org.junit.Test
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource


/**
 * @author encir on 20-Feb-19.
 */
@ContextConfiguration(classes = [Conf::class])
//@RunWith(JPutSpringRunner::class)
@TestPropertySource(properties = ["jput.storage.elastic.enabled:true",
    "jput.storage.elastic.host:localhost"])
class Test {

    @PerformanceTest(maxTimeLimit = 5000L, trends = [
        PerformanceTrend(
                averageTimeThreshold = 1.0,
                sampleSelectionStrategy = SelectionStrategy.USE_FIRST
        )
    ])
    @Test
    @Ignore
    fun test() {
        Thread.sleep(2000)
        println("Hi there")
    }

}

@Configuration
@ComponentScan(basePackages = ["cz.encircled.jput.spring.test"])
open class Conf