package cz.encircled.jput.spring

import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.PropertySource
import cz.encircled.jput.context.context
import cz.encircled.jput.runner.JUnitTestRunnerSupport
import cz.encircled.jput.runner.Junit4TestExecutor
import org.junit.runner.notification.RunNotifier
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.InitializationError
import org.slf4j.LoggerFactory
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
 * @author Vlad on 20-May-17.
 */
class JPutSpringRunner

/**
 * Construct a new `SpringJUnit4ClassRunner` and initialize a
 * [org.springframework.test.context.TestContextManager] to provide Spring testing functionality to
 * standard JUnit tests.
 *
 * @param clazz the test class to be run
 * @see .createTestContextManager
 */
@Throws(InitializationError::class)
constructor(private val clazz: Class<*>) : SpringJUnit4ClassRunner(clazz) {

    val executor = Junit4TestExecutor()

    val log = LoggerFactory.getLogger(JPutSpringRunner::class.java)

    override fun run(notifier: RunNotifier) {
        context = JPutContext()
        context.addPropertySource(object : PropertySource {
            override fun getProperty(key: String): String? {
                return testContextManager.testContext.applicationContext.environment.getProperty(key)
            }
        })

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

        if (isTestMethodIgnored(method)) {
            notifier.fireTestIgnored(description)
        } else {
            executor.executeTest(method, notifier, description, methodBlock(method))
        }
    }

}

