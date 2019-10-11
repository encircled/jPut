package cz.encircled.jput.spring.test

import cz.encircled.jput.JPut
import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.context
import cz.encircled.jput.spring.JPutSpringRunner
import cz.encircled.jput.unit.PerformanceTest
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import kotlin.test.assertEquals
import kotlin.test.assertFalse


/**
 * @author encir on 20-Feb-19.
 */
@ContextConfiguration(classes = [Conf::class])
@RunWith(JPutSpringRunner::class)
@TestPropertySource(properties = ["jput.storage.elastic.enabled:false", "${JPutContext.PROP_STORAGE_FILE_ENABLED}:true",
    "jput.storage.elastic.host:localhost"])
class SpringIntegrationTest {

    @PerformanceTest(maxTimeLimit = 5000L)
    @Test
    fun baseTest(jPut: JPut) {
        jPut.markPerformanceTestStart()
        Thread.sleep(4000)
        println("Hi there")
    }

    @Test
    fun testCurrentSuiteIsSet() {
        assertEquals(this::class.java, context.currentSuite!!.clazz)
        assertFalse(context.currentSuite!!.isParallel)
        assertEquals("testCurrentSuiteIsSet", context.currentSuiteMethod!!.name)
    }

}

@Configuration
@ComponentScan(basePackages = ["cz.encircled.jput.spring.test"])
open class Conf