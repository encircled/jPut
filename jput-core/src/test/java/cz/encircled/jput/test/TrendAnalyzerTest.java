package cz.encircled.jput.test;

import cz.encircled.jput.model.MethodTrendConfiguration;
import cz.encircled.jput.model.PerformanceTestRun;
import cz.encircled.jput.trend.TrendAnalyzerImpl;
import cz.encircled.jput.trend.TrendResult;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Vlad on 27-May-17.
 */
public class TrendAnalyzerTest {

    private TrendAnalyzerImpl trendAnalyzer = new TrendAnalyzerImpl();

    @Test
    public void testCollectRuns() {
        long[] runs = trendAnalyzer.collectRuns(Arrays.asList(TestSupport.getRun(100, 103), TestSupport.getRun(102), TestSupport.getRun(104)));
        Assert.assertArrayEquals(new long[]{100, 103, 102, 104}, runs);
    }

    @Test
    public void testPositiveAverageByVariance() {
        MethodTrendConfiguration conf = new MethodTrendConfiguration().setUseAverageTimeVariance(true);
        PerformanceTestRun testRun = TestSupport.getRun(100);
        assertValid(trendAnalyzer.analyzeTestTrend(conf, testRun, 100, 100, 100));
        assertValid(trendAnalyzer.analyzeTestTrend(conf, testRun, 300, 310, 330));

        // Avg 98.5, var - 1.5
        assertValid(trendAnalyzer.analyzeTestTrend(conf, testRun, 100, 96, 99, 99));
    }

    @Test
    public void testNegativeByVariance() {
        assertAvgNotValid(trendAnalyzer.analyzeTestTrend(new MethodTrendConfiguration().setUseAverageTimeVariance(true), TestSupport.getRun(150), 100, 96, 99, 99));

        // Avg 98.5, var - 1.5
        assertAvgNotValid(trendAnalyzer.analyzeTestTrend(new MethodTrendConfiguration().setUseAverageTimeVariance(true), TestSupport.getRun(101), 100, 96, 99, 99));
    }

    @Test
    public void testPositiveAverageByThreshold() {
        MethodTrendConfiguration conf = new MethodTrendConfiguration().setAverageTimeThreshold(0.1);
        PerformanceTestRun testRun = TestSupport.getRun(100);

        assertValid(trendAnalyzer.analyzeTestTrend(conf, testRun, 100, 100, 100));
        assertValid(trendAnalyzer.analyzeTestTrend(conf, testRun, 300, 310, 330));

        // Avg 100, threshold - 10%
        assertValid(trendAnalyzer.analyzeTestTrend(conf, TestSupport.getRun(110), 95, 105));
    }

    @Test
    public void testNegativeAverageByThreshold() {
        MethodTrendConfiguration conf = new MethodTrendConfiguration().setAverageTimeThreshold(0.1);

        assertAvgNotValid(trendAnalyzer.analyzeTestTrend(conf, TestSupport.getRun(150), 100, 100, 100));

        // Avg 100, threshold - 10%
        assertAvgNotValid(trendAnalyzer.analyzeTestTrend(conf, TestSupport.getRun(111), 95, 105));
    }

    private void assertAvgNotValid(TrendResult trendResult) {
        Assert.assertFalse(trendResult.isAverageMet);
    }

    private void assertValid(TrendResult trendResult) {
        Assert.assertTrue(trendResult.isAverageMet);
        Assert.assertNull(trendResult.notMetPercentiles);
    }

}
