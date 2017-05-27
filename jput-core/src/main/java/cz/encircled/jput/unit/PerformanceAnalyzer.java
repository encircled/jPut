package cz.encircled.jput.unit;

import cz.encircled.jput.model.MethodConfiguration;
import cz.encircled.jput.model.PerformanceTestRun;

import java.lang.reflect.Method;

/**
 * @author Vlad on 21-May-17.
 */
public interface PerformanceAnalyzer {

    PerformanceTestRun build(MethodConfiguration configuration, Method method);

    String toString(PerformanceTestRun run, MethodConfiguration configuration);

    void addRun(PerformanceTestRun run, MethodConfiguration configuration, long elapsedTime);

}
