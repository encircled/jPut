package cz.encircled.jput.reactive

import cz.encircled.jput.runner.JPutJUnit4Runner
import cz.encircled.jput.unit.PerformanceTest
import org.junit.AfterClass
import org.junit.runner.RunWith

/**
 * @author Vlad on 22-Sep-19.
 */
@RunWith(JPutJUnit4Runner::class)
class LoadTest {

    companion object {

        @JvmStatic
        @AfterClass
        fun afterClass() {

        }


    }

    @PerformanceTest(
            parallel = 500
    )
    fun test() {

    }

}