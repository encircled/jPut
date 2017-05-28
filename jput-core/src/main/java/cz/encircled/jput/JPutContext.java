package cz.encircled.jput;

import cz.encircled.jput.trend.TrendAnalyzer;
import cz.encircled.jput.trend.TrendAnalyzerImpl;
import cz.encircled.jput.unit.UnitPerformanceAnalyzer;
import cz.encircled.jput.unit.UnitPerformanceAnalyzerImpl;

/**
 * @author Vlad on 21-May-17.
 */
public class JPutContext {

    private static final JPutContext self = new JPutContext();
    private final long contextExecutionId;
    private UnitPerformanceAnalyzer unitAnalyzer;
    private TrendAnalyzer trendAnalyzer;
    private boolean isPerformanceTestEnabled;

    private JPutContext() {
        String value = System.getProperty("jput.perf.enabled");
        this.isPerformanceTestEnabled = value == null || Boolean.getBoolean(value);
        this.contextExecutionId = System.currentTimeMillis();
        this.unitAnalyzer = new UnitPerformanceAnalyzerImpl();
        this.trendAnalyzer = new TrendAnalyzerImpl();
    }

    public static JPutContext getContext() {
        return self;
    }

    public boolean isPerformanceTestEnabled() {
        return isPerformanceTestEnabled;
    }

    public long getContextExecutionId() {
        return contextExecutionId;
    }

    public UnitPerformanceAnalyzer getUnitPerformanceAnalyzer() {
        return unitAnalyzer;
    }

    public TrendAnalyzer getTrendAnalyzer() {
        return trendAnalyzer;
    }

}
