package cz.encircled.jput.runner

import cz.encircled.jput.context.context
import cz.encircled.jput.unit.PerformanceSuite
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
        context.resultReporter?.beforeClass(clazz)

        // TODO should be extracted as a separate config class later
        val suite = clazz.getAnnotation(PerformanceSuite::class.java)
        if (suite != null && suite.parallel) parallelize(runner)

        context.currentSuite = clazz
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
                try {
                    fService.shutdown();
                    fService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (e: InterruptedException) {
                    e.printStackTrace(System.err);
                }
            }
        });
    }

}