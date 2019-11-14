package cz.encircled.jput.runner

import cz.encircled.jput.annotation.PerformanceSuite
import org.junit.FixMethodOrder
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @author Vlad on 21-Sep-19.
 */
@PerformanceSuite(parallel = true)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(JPutJUnit4Runner::class)
class Junit4TestRunnerParallelTest {

    companion object {
        var stepFinished = false
    }

    @Test
    fun step_1() {
        Thread.sleep(100)
        stepFinished = true
    }

    @Test
    fun z_verify() {
        assertFalse(stepFinished, "step_1 would not finish yet if running in parallel")
    }

}

@PerformanceSuite
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(JPutJUnit4Runner::class)
class Junit4TestRunnerNoParallelTest {

    companion object {
        var stepFinished = false
    }

    @Test
    fun step_1() {
        Thread.sleep(100)
        stepFinished = true
    }

    @Test
    fun z_verify() {
        assertTrue(stepFinished, "step_1 should have finished already if running in sequence")
    }

}