package cz.encircled.jput.unit;

/**
 * @author Vlad on 28-May-17.
 */
public class UnitPerformanceResult {

    public boolean isAverageLimitMet = true;

    public boolean isMaxLimitMet = true;

    public long runAverageTime;

    public long runMaxTime;

    public boolean isError() {
        return !isAverageLimitMet || !isMaxLimitMet;
    }

}
