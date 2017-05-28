package cz.encircled.jput.unit;

import cz.encircled.jput.model.MethodConfiguration;
import cz.encircled.jput.model.PerformanceTestRun;

import java.lang.reflect.Method;

/**
 * @author Vlad on 21-May-17.
 */
public interface UnitPerformanceAnalyzer {

    PerformanceTestRun buildRun(MethodConfiguration configuration, Method method);

    void addRun(PerformanceTestRun run, long elapsedTime);

    UnitPerformanceResult analyzeUnitTrend(PerformanceTestRun run, MethodConfiguration conf);

    String buildErrorMessage(UnitPerformanceResult result, MethodConfiguration conf);

}
