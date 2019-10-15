package cz.encircled.jput.runner

import cz.encircled.jput.JPut
import cz.encircled.jput.TestReporter
import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.context
import cz.encircled.jput.runner.steps.Junit4TestRunnerSteps
import cz.encircled.jput.unit.PerformanceTest
import org.junit.BeforeClass
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener
import org.junit.runner.notification.RunNotifier
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource

import kotlin.test.*

class TestRunListener : RunListener() {

    val failures = mutableListOf<Failure>()

    val finished = mutableListOf<Description>()

    override fun testAssumptionFailure(failure: Failure?) {
        super.testAssumptionFailure(failure)
    }

    override fun testFinished(description: Description) {
        finished.add(description)
    }

    override fun testFailure(failure: Failure) {
        failures.add(failure)
    }

}

class Junit4TestRunnerIntegrationTest {

    companion object {

        val listener = TestRunListener()

        @JvmStatic
        @BeforeClass
        fun runSteps() {
            val runner = JPutJUnit4Runner(Junit4TestRunnerSteps::class.java)
            val notifier = RunNotifier()
            notifier.addListener(listener)
            runner.run(notifier)
        }

    }

    @Test
    fun testWarmUpAndRepeatsCount() {
        assertSuccessful("testWarmUpAndRepeatsCount")
        assertEquals(4, Junit4TestRunnerSteps.testCounter)
    }

    @Test
    fun testIgnoredTest() {
        assertFalse(isTestExecuted("testIgnoredTest"))
    }

    @Test
    fun testCurrentSuiteIsSet() {
        assertEquals(Junit4TestRunnerSteps::class.java, context.currentSuite!!.clazz)
        assertFalse(context.currentSuite!!.isParallel)
    }

    @Test
    fun testReturnedErrorPropagated() {
        val expected = "Performance test failed.\n[Limit exceptions count = 1, actual = 2]"
        assertFailedAssertion("testReturnedErrorPropagated", expected)
    }

    @Test
    fun testRuntimeExceptionCatched() {
        val expected = "Performance test failed.\n[Limit exceptions count = 1, actual = 2]"
        assertFailedAssertion("testRuntimeExceptionCatched", expected)
    }

    @Test
    fun testReporterIsInvokedCorrectly() {
        val reporter = context.resultReporters[1] as TestReporter

        assertEquals(
                mutableListOf<Pair<String, Any?>>(
                        "beforeClass" to Junit4TestRunnerSteps::class.java,

                        "beforeTest" to "Junit4TestRunnerSteps#testReturnedErrorPropagated",
                        "afterTest" to "Junit4TestRunnerSteps#testReturnedErrorPropagated",

                        "beforeTest" to "Junit4TestRunnerSteps#testRuntimeExceptionCatched",
                        "afterTest" to "Junit4TestRunnerSteps#testRuntimeExceptionCatched",

                        "beforeTest" to "Junit4TestRunnerSteps#testWarmUpAndRepeatsCount",
                        "afterTest" to "Junit4TestRunnerSteps#testWarmUpAndRepeatsCount",

                        "afterClass" to Junit4TestRunnerSteps::class.java
                ),
                reporter.invocations
        )
    }

    /**
     * Assert that unit test failed with given [expectedAssertion]
     */
    fun assertFailedAssertion(method: String, expectedAssertion: String) {
        val failure = expectFailure(method)
        assertEquals(expectedAssertion, failure.exception.message)
    }

    private fun assertSuccessful(method: String) {
        assertTrue(listener.failures
                .map { it.description.methodName }
                .all { it != method })

        assertTrue(isTestExecuted(method))
    }

    private fun expectFailure(method: String): Failure =
            listener.failures.firstOrNull {
                it.description.methodName == method
            } ?: fail("Assertion failure is expected for $method")

    private fun isTestExecuted(method: String): Boolean {
        return listener.finished
                .map { it.methodName }
                .any() { it == method }
    }

}

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