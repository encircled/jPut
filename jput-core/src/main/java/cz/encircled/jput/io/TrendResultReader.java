package cz.encircled.jput.io;

import cz.encircled.jput.model.PerformanceTestRun;

import java.util.Collection;

/**
 * @author Vlad on 21-May-17.
 */
public interface TrendResultReader {

    Collection<PerformanceTestRun> getTestStatistics(PerformanceTestRun newRun);

}
