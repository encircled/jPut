package cz.encircled.jput.test;

import cz.encircled.jput.JPutContext;
import cz.encircled.jput.model.PerformanceTestRun;

/**
 * @author Vlad on 27-May-17.
 */
public class TestSupport {

    public static PerformanceTestRun getRun(long... times) {
        PerformanceTestRun run = new PerformanceTestRun();
        run.runs = times;
        run.executionId = JPutContext.getContext().getContextExecutionId();
        return run;
    }

}
