package cz.encircled.jput.model;

/**
 * @author Vlad on 20-May-17.
 */
public class PerformanceTestRun {

    public long executionId;

    public String testClass;

    public String testMethod;

    public long[] runs;

    public transient int positionCounter = 0;

    public PerformanceTestRun() {

    }

}
