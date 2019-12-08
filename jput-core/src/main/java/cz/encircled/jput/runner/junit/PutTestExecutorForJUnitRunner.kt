package cz.encircled.jput.runner.junit

import cz.encircled.jput.JPut
import cz.encircled.jput.annotation.PerformanceTest
import cz.encircled.jput.context.ConfigurationBuilder
import cz.encircled.jput.context.context
import cz.encircled.jput.runner.BaseTestExecutor
import cz.encircled.jput.runner.ThreadBasedTestExecutor
import junit.framework.AssertionFailedError
import org.junit.AssumptionViolatedException
import org.junit.internal.runners.model.EachTestNotifier
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement

/**
 * All JUnit runners should delegate execution to this class
 *
 * @author Vlad on 22-Sep-19.
 */
class PutTestExecutorForJUnitRunner {

    private var realExecutor: BaseTestExecutor = ThreadBasedTestExecutor()

    var isInitialized = false

    companion object {

        /**
         * Hack for [InvokeMethodWithParams] - there is no other easy way how to pass the [JPut] argument to the unit test
         * and at the same time re-use existing JUnit Statements (like BeforeTest, AfterTest etc)
         */
        val jPut = ThreadLocal<JPut?>()

        /**
         * Hack for [InvokeMethodWithParams] - there is no other easy way how to receive return value from the unit test
         * and re-use existing JUnit Statements (like BeforeTest, AfterTest etc)
         */
        val result = ThreadLocal<Any?>()
    }

    init {
        // TODO pick executor in a better way?
        try {
            val reactive = Class.forName("cz.encircled.jput.reactive.ReactiveTestExecutor")
            realExecutor = reactive.getConstructor().newInstance() as BaseTestExecutor
        } catch (e: ClassNotFoundException) {
            // OK
        }
    }

    fun executeTest(method: FrameworkMethod, notifier: RunNotifier, description: Description, statement: Statement) {
        val eachNotifier = EachTestNotifier(notifier, description)
        eachNotifier.fireTestStarted()

        try {
            val annotation = method.getAnnotation(PerformanceTest::class.java)

            if (context.isPerformanceTestEnabled && annotation != null) runJPutTest(annotation, method, statement)
            else statement.evaluate()
        } catch (e: Throwable) {
            if (e.cause is AssumptionViolatedException) eachNotifier.addFailedAssumption(e.cause as AssumptionViolatedException)
            else eachNotifier.addFailure(e)
        } finally {
            eachNotifier.fireTestFinished()
        }
    }

    private fun runJPutTest(annotation: PerformanceTest, method: FrameworkMethod, statement: Statement) {
        val conf = ConfigurationBuilder.buildConfig(annotation, method.method)

        val execution = realExecutor.executeTest(conf) {
            jPut.set(it)
            statement.evaluate()
            result.get()
        }

        if (execution.violations.isNotEmpty()) {
            throw AssertionFailedError("Performance test failed.\n${execution.violationsErrorMessage}")
        }
    }

}