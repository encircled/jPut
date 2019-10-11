package cz.encircled.jput.runner

import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.context
import org.junit.Test
import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement

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

    /**
     * Skip args/return type validation of performance tests
     */
    override fun validatePublicVoidNoArgMethods(annotation: Class<out Annotation>, isStatic: Boolean, errors: MutableList<Throwable>) {
        if (annotation != Test::class.java) {
            super.validatePublicVoidNoArgMethods(annotation, isStatic, errors)
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

    /**
     * Allows passing [cz.encircled.jput.JPut] to the test
     */
    override fun methodInvoker(method: FrameworkMethod, test: Any): Statement {
        return InvokeMethodWithParams(test, method)
    }

}