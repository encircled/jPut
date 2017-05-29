package cz.encircled.jput.trend;

import cz.encircled.jput.model.MethodTrendConfiguration;
import cz.encircled.jput.model.PerformanceTestRun;

/**
 * @author Vlad on 21-May-17.
 */
public interface TrendAnalyzer {

    TrendResult analyzeTestTrend(MethodTrendConfiguration configuration, PerformanceTestRun testRun, long... standardSampleRuns);

}
