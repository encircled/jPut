package cz.encircled.jput.test

import cz.encircled.jput.deviation
import cz.encircled.jput.percentile
import cz.encircled.jput.variance
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertEquals

/**
 * @author Vlad on 27-May-17.
 */
class StatisticsTest {

    @Test
    fun testVariance() {
        Assert.assertEquals(4.0, listOf(1L, 5).variance(), 0.0)
        Assert.assertEquals(1.25, listOf(2L, 3, 4, 5).variance(), 0.0)
        Assert.assertEquals(200.0, listOf(50L, 60, 70, 80, 90).variance(), 0.0)
    }

    @Test
    fun testDeviation() {
        Assert.assertEquals(2.0, listOf(1L, 5).deviation(), 0.0)
    }

    @Test(expected = IllegalStateException::class)
    fun testArrayPercentileValid() {
        listOf(1L, 10).percentile(1.01)
    }

    @Test(expected = IllegalStateException::class)
    fun testArrayPercentileValidLower() {
        listOf(1L, 10).percentile(-0.1)
    }

    @Test
    fun testArrayPercentile() {
        val res = listOf(1L, 2, 3, 4, 5, 6, 7, 8, 9, 10).percentile(0.9)
        assertEquals(listOf<Long>(1, 2, 3, 4, 5, 6, 7, 8, 9), res)
    }

    @Test
    fun testArrayPercentileRound() {
        assertEquals(listOf<Long>(1, 2, 3, 4, 5, 6, 7, 8, 9), listOf(1L, 2, 3, 4, 5, 6, 7, 8, 9, 10).percentile(0.94))
        assertEquals(listOf<Long>(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), listOf(1L, 2, 3, 4, 5, 6, 7, 8, 9, 10).percentile(0.95))
    }

}
