package cz.encircled.jput.trend;

import cz.encircled.jput.Statistics;
import cz.encircled.jput.model.MethodConfiguration;
import cz.encircled.jput.model.MethodTrendConfiguration;
import cz.encircled.jput.model.PerformanceTestRun;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Vlad on 27-May-17.
 */
public class StandardSampleTrendAnalyzer implements TrendAnalyzer {

    @Override
    public TrendResult analyzeTestTrend(MethodTrendConfiguration configuration, PerformanceTestRun testRun, long... standardSampleRuns) {
        if (standardSampleRuns == null) {
            return new TrendResult();
        }
        Arrays.sort(standardSampleRuns);
        if (configuration.noisePercentilePercentile > 0) {
            standardSampleRuns = Statistics.getPercentile(standardSampleRuns, configuration.noisePercentilePercentile);
        }

        if (configuration.measureAverageTime) {
            double standardAverage = Statistics.getAverage(standardSampleRuns);

            double deviation = configuration.useAverageTimeVariance ?
                    Statistics.getStandardDeviation(standardSampleRuns) :
                    standardAverage * configuration.averageTimeThreshold; // TODO add validation > 0

            double runMean = Statistics.getAverage(testRun.runs); // TODO percentile?
            if (runMean > standardAverage + deviation) {
                TrendResult trendResult = new TrendResult();
                trendResult.isAverageMet = false;
                trendResult.runAverageTime = Statistics.round(runMean);
                trendResult.deviation = Statistics.round(deviation);
                trendResult.standardAverage = Statistics.round(standardAverage);
                return trendResult;
            }
        }

        return new TrendResult();
    }

    @Override
    public String buildErrorMessage(TrendResult result, MethodConfiguration conf) {
        String msg = "";
        if (!result.isAverageMet) {
            msg += String.format("\nStandard avg time = %d ms (%d + %d deviation) \nActual avg time = %d ms",
                    result.standardAverage + result.deviation, result.standardAverage, result.deviation, result.runAverageTime);
        }
        /*if (!result.isMaxLimitMet) {
            if (!result.isAverageLimitMet) {
                msg += "\n";
            }
            msg += String.format("\nLimit max time = %d ms\nActual max time = %d ms", conf.maxTimeLimit, 0);// TODO
        }*/
        return msg;
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

}
