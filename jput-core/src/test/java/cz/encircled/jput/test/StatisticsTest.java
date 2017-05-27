package cz.encircled.jput.test;

import cz.encircled.jput.Statistics;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vlad on 27-May-17.
 */
public class StatisticsTest {

    @Test
    public void testAverage() {
        Assert.assertEquals(2, Statistics.getAverage(1, 3), 0);
        Assert.assertEquals(6, Statistics.getAverage(4, 6, 8), 0);
        Assert.assertEquals(98.5, Statistics.getAverage(100, 96, 99, 99), 0);
    }

    @Test
    public void testVariance() {
        Assert.assertEquals(1.0, Statistics.getVariance(1, 3), 0);
    }

    @Test
    public void testDeviation() {
        Assert.assertEquals(2.0, Statistics.getStandardDeviation(1, 5), 0);
    }

    @Test(expected = IllegalStateException.class)
    public void testArrayPercentileValid() {
        Statistics.getPercentile(new long[]{1, 10}, 1.01);
    }

    @Test(expected = IllegalStateException.class)
    public void testArrayPercentileValidLower() {
        Statistics.getPercentile(new long[]{1, 10}, -0.1);
    }

    @Test
    public void testArrayPercentile() {
        Assert.assertArrayEquals(new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9}, Statistics.getPercentile(new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 0.9));
    }

    @Test
    public void testArrayPercentileRound() {
        Assert.assertArrayEquals(new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9}, Statistics.getPercentile(new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 0.94));
        Assert.assertArrayEquals(new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, Statistics.getPercentile(new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 0.95));
    }

}
