package cz.encircled.jput.spring

import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.PropertySource
import cz.encircled.jput.context.context
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
constructor(clazz: Class<*>) : SpringJUnit4ClassRunner(clazz) {

    val log = LoggerFactory.getLogger(JPutSpringRunner::class.java)

    override fun run(notifier: RunNotifier) {
        context = JPutContext()
        context.addPropertySource(object : PropertySource {
            override fun getProperty(key: String): String? {
                return testContextManager.testContext.applicationContext.environment.getProperty(key)
            }
        })
        context.init()

        try {
            super.run(notifier)
        } finally {
            context.destroy()
        }
    }

    override fun runChild(method: FrameworkMethod, notifier: RunNotifier) {
        val description = describeChild(method)

        if (isTestMethodIgnored(method)) {
            notifier.fireTestIgnored(description)
        } else {
            context.junit4TestExecutor.executeTest(method, notifier, description, methodBlock(method))
        }
    }

}

