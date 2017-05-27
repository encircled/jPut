package cz.encircled.jput;

/**
 * @author Vlad on 21-May-17.
 */
public class JPutContext {

    private static final JPutContext self = new JPutContext();
    private final long contextExecutionId;
    private boolean isPerformanceTestEnabled;

    private JPutContext() {
        String value = System.getProperty("jput.perf.enabled");
        this.isPerformanceTestEnabled = value == null || Boolean.getBoolean(value);
        this.contextExecutionId = System.currentTimeMillis();
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
}
