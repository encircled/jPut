package cz.encircled.jput.io;

import cz.encircled.jput.model.PerformanceTestRun;

/**
 * @author Vlad on 21-May-17.
 */
public interface TrendResultWriter {

    void appendTrendResult(PerformanceTestRun run);

    void flush();

}
