package cz.encircled.jput.runner

import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.context
import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod

/**
 * [org.junit.runner.Runner] impl for JUnit 4
 */
class JPutJUnit4Runner(private val clazz: Class<*>) : BlockJUnit4ClassRunner(clazz) {

    var executor = Junit4TestExecutor()

    override fun run(notifier: RunNotifier?) {
        context = JPutContext()
        JUnitTestRunnerSupport(clazz).prepareRunner(this)

        try {
            super.run(notifier)
        } finally {
            context.destroy()
            context.resultReporters.forEach { it.afterClass(clazz) }
        }
    }

    override fun runChild(method: FrameworkMethod, notifier: RunNotifier) {
        context.currentSuiteMethod = method.method
        val description = describeChild(method)

        if (this.isIgnored(method)) run {
            notifier.fireTestIgnored(description)
        } else {
            executor.executeTest(method, notifier, description, methodBlock(method))
        }
    }

}