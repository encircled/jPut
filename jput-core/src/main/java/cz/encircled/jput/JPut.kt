package cz.encircled.jput

import cz.encircled.jput.model.ExecutionRun

/**
 * TODO list:
 * - Timeout when max time is not set
 * - Self warm up
 * - Logging
 * - Fix when parallel > repeats
 * - Error on warmup
 *
 * Helper functions for JPut users to control the perf tests execution
 *
 * @author Vlad on 15-Sep-19.
 */
interface JPut {

    /**
     * This function may be called directly from the performance test to tell JPut that measurement must start from this point,
     * i.e. not from the beginning of the method. This might be useful when performance test makes some expensive initialization first.
     *
     * For example:
     *
     * ```
     * @PerformanceTest(...)
     * public void myPerfTest(JPut jPut) {
     *     prepareTestData(); // takes some time...
     *
     *     jPut.markPerformanceTestStart(); // Ignore time took by the init
     *
     *     // and here goes the code which will really be measured
     *     ...
     * }
     * ```
     */
    fun markPerformanceTestStart()

    /**
     * This function may be called directly from the performance test to tell JPut that time measurement must end at this point,
     * i.e. not until the end of the method. This might be useful when performance test makes some expensive assertions/cleanup after each test repeat.
     *
     * For example:
     *
     * ```
     * @PerformanceTest(...)
     * public void myPerfTest(JPut jPut) {
     *     // here goes code which is actually being measured
     *
     *     jPut.markPerformanceTestEnd(); // Ignore time taken after this point
     *
     *     // some expensive assertions, cleanup etc
     * }
     * ```
     */
    fun markPerformanceTestEnd()

}

class JPutImpl(private val executionRun: ExecutionRun) : JPut {

    override fun markPerformanceTestStart() {
        executionRun.relativeStartTime = System.nanoTime() - executionRun.execution.startTime
    }

    override fun markPerformanceTestEnd() {
        executionRun.measureElapsed()
    }

}