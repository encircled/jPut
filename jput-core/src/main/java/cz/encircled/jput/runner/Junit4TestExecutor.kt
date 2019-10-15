package cz.encircled.jput.runner

import cz.encircled.jput.context.ConfigurationBuilder
import cz.encircled.jput.context.context
import cz.encircled.jput.unit.PerformanceTest
import junit.framework.AssertionFailedError
import org.junit.AssumptionViolatedException
import org.junit.internal.runners.model.EachTestNotifier
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement

/**
 * TODO redesign executors / inheritance / composition
 *
 * @author Vlad on 22-Sep-19.
 */
class Junit4TestExecutor {

    var executor: ThreadBasedTestExecutor = ThreadBasedTestExecutor()

    init {
        // TODO pick executor in a better way?
        try {
            val reactive = Class.forName("cz.encircled.jput.reactive.ReactiveTestExecutor")
            executor = reactive.getConstructor().newInstance() as ThreadBasedTestExecutor
        } catch (e: ClassNotFoundException) {
            // OK
        }
    }

    fun executeTest(method: FrameworkMethod, notifier: RunNotifier, description: Description, statement: Statement) {
        val eachNotifier = EachTestNotifier(notifier, description)
        eachNotifier.fireTestStarted()

        try {
            val annotation = method.getAnnotation(PerformanceTest::class.java)
            if (annotation == null || !context.isPerformanceTestEnabled) {
                statement.evaluate()
            } else {
                val conf = ConfigurationBuilder.buildConfig(annotation, method.method)

                val execution = executor.executeTest(conf) {
                    if (statement is InvokeMethodWithParams) statement.evaluateWithParams(it) else statement.evaluate()
                }

                if (execution.violations.isNotEmpty()) {
                    throw AssertionFailedError("Performance test failed.\n${execution.violationsErrorMessage}")
                }
            }
        } catch (e: Throwable) {
            if (e.cause is AssumptionViolatedException) eachNotifier.addFailedAssumption(e.cause as AssumptionViolatedException)
            else eachNotifier.addFailure(e)
        } finally {
            eachNotifier.fireTestFinished()
        }
    }

}