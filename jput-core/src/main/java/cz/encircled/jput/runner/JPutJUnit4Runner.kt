package cz.encircled.jput.runner

import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.context
import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.unit.PerformanceTest
import junit.framework.AssertionFailedError
import org.junit.AssumptionViolatedException
import org.junit.internal.runners.model.EachTestNotifier
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement

class Junit4TestExecutor : BaseTestExecutor() {

    fun executeTest(method: FrameworkMethod, notifier: RunNotifier, description: Description, statement: Statement) {
        val eachNotifier = EachTestNotifier(notifier, description)
        eachNotifier.fireTestStarted()

        try {
            val annotation = method.getAnnotation(PerformanceTest::class.java)
            if (annotation == null || !context.isPerformanceTestEnabled) {
                statement.evaluate()
            } else {
                val conf = PerfTestConfiguration.fromAnnotation(annotation, method.method)

                val execution = executeTest(conf) { statement.evaluate() }

                if (execution.result!!.isError) {
                    throw AssertionFailedError("Performance test failed.\n${execution.result!!.violations}")
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

class JPutJUnit4Runner(clazz: Class<*>) : BlockJUnit4ClassRunner(clazz) {

    override fun run(notifier: RunNotifier?) {
        context = JPutContext()
        context.init()
        super.run(notifier)
    }

    override fun runChild(method: FrameworkMethod, notifier: RunNotifier) {
        val description = describeChild(method)

        if (this.isIgnored(method)) run {
            notifier.fireTestIgnored(description)
        } else {
            context.junit4TestExecutor.executeTest(method, notifier, description, methodBlock(method))
        }
    }

}