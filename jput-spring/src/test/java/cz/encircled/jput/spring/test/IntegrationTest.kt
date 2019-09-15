package cz.encircled.jput.spring.test

import cz.encircled.jput.spring.JPutSpringRunner
import cz.encircled.jput.unit.PerformanceTest
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource


/**
 * @author encir on 20-Feb-19.
 */
@ContextConfiguration(classes = [Conf::class])
@RunWith(JPutSpringRunner::class)
@TestPropertySource(properties = ["jput.storage.elastic.enabled:false",
    "jput.storage.elastic.host:localhost"])
class SpringIntegrationTest {

    @PerformanceTest(maxTimeLimit = 5000L)
    @Test
    fun baseTest() {
        Thread.sleep(4000)
        println("Hi there")
    }

}

@Configuration
@ComponentScan(basePackages = ["cz.encircled.jput.spring.test"])
open class Conf