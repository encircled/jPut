package cz.encircled.jput.test

import cz.encircled.jput.Statistics
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertEquals

/**
 * @author Vlad on 27-May-17.
 */
class StatisticsTest {

    @Test
    fun testAverage() {
        Assert.assertEquals(2.0, Statistics.getAverage(listOf(1, 3)), 0.0)
        Assert.assertEquals(6.0, Statistics.getAverage(listOf(4, 6, 8)), 0.0)
        Assert.assertEquals(98.5, Statistics.getAverage(listOf(100, 96, 99, 99)), 0.0)
    }

    @Test
    fun testVariance() {
        Assert.assertEquals(1.0, Statistics.getStandardDeviation(listOf(1, 3)), 0.0)
    }

    @Test
    fun testDeviation() {
        Assert.assertEquals(2.0, Statistics.getVariance(listOf(1, 5)), 0.0)
    }

    @Test(expected = IllegalStateException::class)
    fun testArrayPercentileValid() {
        Statistics.getPercentile(listOf(1, 10), 1.01)
    }

    @Test(expected = IllegalStateException::class)
    fun testArrayPercentileValidLower() {
        Statistics.getPercentile(listOf(1, 10), -0.1)
    }

    @Test
    fun testArrayPercentile() {
        assertEquals(listOf<Long>(1, 2, 3, 4, 5, 6, 7, 8, 9), Statistics.getPercentile(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 0.9))
    }

    @Test
    fun testArrayPercentileRound() {
        assertEquals(listOf<Long>(1, 2, 3, 4, 5, 6, 7, 8, 9), Statistics.getPercentile(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 0.94))
        assertEquals(listOf<Long>(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), Statistics.getPercentile(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 0.95))
    }

}
