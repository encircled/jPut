package cz.encircled.jput.trend;

/**
 * @author Vlad on 27-May-17.
 */
public class TrendResult {

    public boolean isAverageMet = true;

    public long runAverageTime;

    public long standardAverage;

    public long deviation;

    public int[] notMetPercentiles;

    public boolean isError() {
        return !isAverageMet || (notMetPercentiles != null && notMetPercentiles.length > 0);
    }
}
