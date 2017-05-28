package cz.encircled.jput.test;

import cz.encircled.jput.JPutContext;
import cz.encircled.jput.model.PerformanceTestRun;

import java.lang.reflect.Method;

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

    public static PerformanceTestRun getRun(Method method, long... times) {
        PerformanceTestRun run = new PerformanceTestRun();
        run.runs = times;
        run.executionId = JPutContext.getContext().getContextExecutionId();
        run.testMethod = method.getName();
        run.testClass = method.getDeclaringClass().getName();
        return run;
    }

}
