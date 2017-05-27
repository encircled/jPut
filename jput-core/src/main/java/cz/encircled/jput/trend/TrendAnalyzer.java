package cz.encircled.jput.trend;

import cz.encircled.jput.model.MethodTrendConfiguration;
import cz.encircled.jput.model.PerformanceTestRun;

import java.util.Collection;

/**
 * @author Vlad on 21-May-17.
 */
public interface TrendAnalyzer {

    long[] filterPercentile(Collection<PerformanceTestRun> runs, int percentile);

    TrendResult analyzeTestTrend(MethodTrendConfiguration configuration, PerformanceTestRun testRun, long... standardSampleRuns);

}
