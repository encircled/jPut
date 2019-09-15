package cz.encircled.jput

import cz.encircled.jput.context.context
import cz.encircled.jput.model.PerfTestConfiguration
import org.slf4j.LoggerFactory
import java.lang.reflect.Method

/**
 * Helper functions for JPut users to control the perf tests execution
 *
 * @author encir on 15-Sep-19.
 */
public object JPut {

    private val log = LoggerFactory.getLogger(JPut::class.java)

    /**
     * This function may be called directly from the performance test to tell JPut that measurement must start from this point,
     * i.e. not from the beginning of the method. This might be useful when performance test makes some expensive initialization first.
     *
     * For example:
     *
     * ```
     * @PerformanceTest(...)
     * public void myPerfTest() {
     *     prepareTestData(); // takes some time...
     *
     *     JPut.markPerformanceTestStart(); // Ignore time took by the init
     *
     *     // and here goes the code which will really be measured
     *     ...
     * }
     * ```
     */
    fun markPerformanceTestStart() {
        val caller = Thread.currentThread().stackTrace[2]
        val defaultTestId = PerfTestConfiguration.defaultTestId(toMethod(caller))

        val testId = context.customTestIds[defaultTestId] ?: defaultTestId
        val execution = context.testExecutions[testId]
        if (execution == null) {
            log.warn("[$caller] is not a JPut test, ignoring [markPerformanceTestStart]")
        } else {
            execution.startNextExecution()
        }
    }

    private fun toMethod(element: StackTraceElement): Method =
            Class.forName(element.className).getDeclaredMethod(element.methodName)

}