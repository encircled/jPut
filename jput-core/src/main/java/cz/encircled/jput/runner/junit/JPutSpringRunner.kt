package cz.encircled.jput.runner.junit

import cz.encircled.jput.context.PropertySource
import cz.encircled.jput.context.context
import org.junit.Test
import org.junit.runner.notification.RunNotifier
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
 * Construct a new `SpringJUnit4ClassRunner` and initialize a
 * [org.springframework.test.context.TestContextManager] to provide JPut and Spring testing functionality to
 * standard JUnit tests.
 *
 * @param clazz the test class to be run
 */
class JPutSpringRunner(private val clazz: Class<*>) : SpringJUnit4ClassRunner(clazz) {

    private val executor = PutTestExecutorForJUnitRunner()

    override fun run(notifier: RunNotifier) {
        if (!executor.isInitialized) {
            executor.isInitialized = true
            context.addPropertySource(object : PropertySource {
                override fun getProperty(key: String): String? {
                    return testContextManager.testContext.applicationContext.environment.getProperty(key)
                }
            })

            context.init()
        }

        JUnitTestRunnerSupport(clazz).prepareRunner(this)

        try {
            super.run(notifier)
        } finally {
            context.afterTestClass(clazz)
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

        if (isTestMethodIgnored(method)) {
            notifier.fireTestIgnored(description)
        } else {
            executor.executeTest(method, notifier, description, methodBlock(method))
        }
    }

    /**
     * Allows passing [cz.encircled.jput.JPut] to the test
     */
    override fun methodInvoker(method: FrameworkMethod, test: Any): Statement = InvokeMethodWithParams(test, method)

}

