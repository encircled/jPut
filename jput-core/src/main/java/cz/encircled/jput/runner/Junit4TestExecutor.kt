package cz.encircled.jput.runner

import cz.encircled.jput.context.context
import cz.encircled.jput.model.PerfTestConfiguration
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

    // TODO pick executor
    val executor: ThreadTestExecutor = ThreadTestExecutor()

    fun executeTest(method: FrameworkMethod, notifier: RunNotifier, description: Description, statement: Statement) {
        val eachNotifier = EachTestNotifier(notifier, description)
        eachNotifier.fireTestStarted()

        try {
            val annotation = method.getAnnotation(PerformanceTest::class.java)
            if (annotation == null || !context.isPerformanceTestEnabled) {
                statement.evaluate()
            } else {
                val conf = PerfTestConfiguration.fromAnnotation(annotation, method.method)

                val execution = executor.executeTest(conf) { statement.evaluate() }

                if (execution.result!!.isError) {
                    // TODO no tests for messages
                    val violations = execution.result!!.violations.map {
                        it.messageProducer.invoke(execution)
                    }
                    throw AssertionFailedError("Performance test failed.\n$violations")
                }
            }
        } catch (e: AssumptionViolatedException) {
            eachNotifier.addFailedAssumption(e)
        } catch (e: Throwable) {
            eachNotifier.addFailure(e)
        } finally {
            eachNotifier.fireTestFinished()
        }
    }

}