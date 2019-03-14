package cz.encircled.jput

import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.context
import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod

class JPutJUnitRunner(clazz: Class<*>) : GenericJunitRunner by (GenericJunitRunnerImpl()), BlockJUnit4ClassRunner(clazz) {

    override fun run(notifier: RunNotifier?) {
        context = JPutContext()
        context.init()
        super.run(notifier)
    }

    override fun runChild(method: FrameworkMethod, notifier: RunNotifier) {
        if (this.isIgnored(method)) run {
            notifier.fireTestIgnored(description)
        } else {
            val description = describeChild(method)

            executeTest(method, description, methodBlock(method), notifier)
        }
    }

}