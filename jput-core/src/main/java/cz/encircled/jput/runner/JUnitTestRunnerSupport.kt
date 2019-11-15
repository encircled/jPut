package cz.encircled.jput.runner

import cz.encircled.jput.annotation.PerformanceSuite
import cz.encircled.jput.context.context
import cz.encircled.jput.model.SuiteConfiguration
import org.junit.runners.ParentRunner
import org.junit.runners.model.RunnerScheduler
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author Vlad on 22-Sep-19.
 */
class JUnitTestRunnerSupport(private val clazz: Class<*>) {

    fun prepareRunner(runner: ParentRunner<*>) {
        context.init()
        context.resultReporters.forEach { it.beforeClass(clazz) }

        val suite = clazz.getAnnotation(PerformanceSuite::class.java)
        context.currentSuite = SuiteConfiguration.fromAnnotation(clazz, suite)

        if (context.currentSuite!!.isParallel) parallelize(runner)
    }

    /**
     * Set parallel junit scheduler
     */
    private fun parallelize(runner: ParentRunner<*>) {
        runner.setScheduler(object : RunnerScheduler {
            val fService: ExecutorService = Executors.newCachedThreadPool()

            override fun schedule(childStatement: Runnable) {
                fService.submit(childStatement)
            }

            override fun finished() {
                fService.shutdown()
                fService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
            }
        })
    }

}