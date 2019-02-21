package cz.encircled.jput.test

import cz.encircled.jput.spring.JPutSpringRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration

/**
 * @author encir on 20-Feb-19.
 */
@ContextConfiguration(classes = [Conf::class])
@RunWith(JPutSpringRunner::class)
class Test {

    //    @PerformanceTest(maxTimeLimit = 1000L, performanceTrend = [PerformanceTrend(averageTimeThreshold = 1.0)])
    @Test
    fun test() {
//        Thread.sleep(2000L)
    }

}

@Configuration
@ComponentScan(basePackages = ["cz.encircled.jput.test"])
open class Conf {

}