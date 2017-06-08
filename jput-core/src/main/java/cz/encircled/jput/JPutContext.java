package cz.encircled.jput;

import cz.encircled.jput.io.TrendResultReader;
import cz.encircled.jput.io.TrendResultWriter;
import cz.encircled.jput.io.file.FileSystemResultReader;
import cz.encircled.jput.io.file.FileSystemResultWriter;
import cz.encircled.jput.model.CommonConstants;
import cz.encircled.jput.trend.StandardSampleTrendAnalyzer;
import cz.encircled.jput.trend.TrendAnalyzer;
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
    private TrendResultReader trendResultReader;
    private TrendResultWriter trendResultWriter;
    private boolean isPerformanceTestEnabled;

    private JPutContext() {
        this.isPerformanceTestEnabled = getBoolPropertyOrDefault(CommonConstants.PROP_ENABLED, true);
        this.contextExecutionId = System.currentTimeMillis();
        this.unitAnalyzer = new UnitPerformanceAnalyzerImpl();
        this.trendAnalyzer = new StandardSampleTrendAnalyzer();

        String pathToFile = System.getProperty("java.io.tmpdir") + "jput-test.data";
        this.trendResultReader = new FileSystemResultReader(pathToFile); // TODO
        this.trendResultWriter = new FileSystemResultWriter(pathToFile); // TODO
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

    public TrendResultReader getTrendResultReader() {
        return trendResultReader;
    }

    public TrendResultWriter getTrendResultWriter() {
        return trendResultWriter;
    }

    private String getProperty(String key, String defaultVal) {
        String value = System.getProperty(key);
        if (value == null) {
            throw new IllegalStateException("JPut property [" + key + "] is mandatory");
        }
        return value;
    }

    private String getPropertyOrDefault(String key, String defaultVal) {
        String value = System.getProperty(key);
        if (value == null) {
            return defaultVal;
        }
        return value;
    }

    private boolean getBoolPropertyOrDefault(String key, boolean defaultVal) {
        String value = System.getProperty(key);
        if (value == null) {
            return defaultVal;
        }
        return Boolean.getBoolean(value);
    }

}
