package cz.encircled.jput.trend;

import cz.encircled.jput.Statistics;
import cz.encircled.jput.model.MethodTrendConfiguration;
import cz.encircled.jput.model.PerformanceTestRun;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Vlad on 27-May-17.
 */
public class TrendAnalyzerImpl implements TrendAnalyzer {

    @Override
    public long[] filterPercentile(Collection<PerformanceTestRun> runs, int percentile) {
        return new long[0];
    }

    public long[] collectRuns(Collection<PerformanceTestRun> standardSampleRuns) {
        int pos = 0;

        int totalLength = 0;
        for (PerformanceTestRun run : standardSampleRuns) {
            totalLength += run.runs.length;
        }

        long result[] = new long[totalLength];

        for (PerformanceTestRun run : standardSampleRuns) {
            System.arraycopy(run.runs, 0, result, pos, run.runs.length);
            pos += run.runs.length;
        }

        return result;
    }

    @Override
    public TrendResult analyzeTestTrend(MethodTrendConfiguration configuration, PerformanceTestRun testRun, long... standardSampleRuns) {
        Arrays.sort(standardSampleRuns);
        if (configuration.noisePercentilePercentile > 0) {
            standardSampleRuns = Statistics.getPercentile(standardSampleRuns, configuration.noisePercentilePercentile);
        }

        if (configuration.measureAverageTime) {
            double standardAverage = Statistics.getAverage(standardSampleRuns);

            double deviation = configuration.useAverageTimeVariance ?
                    Statistics.getStandardDeviation(standardSampleRuns) :
                    standardAverage * configuration.averageTimeThreshold;

            double runMean = Statistics.getAverage(testRun.runs); // TODO percentile?
            if (runMean > standardAverage + deviation) {
                TrendResult trendResult = new TrendResult();
                trendResult.isAverageMet = false;
                return trendResult;
            }
        }

        return new TrendResult();
    }

}
