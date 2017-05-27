package cz.encircled.jput.test;

import cz.encircled.jput.io.file.FileSystemResultWriter;
import cz.encircled.jput.model.MethodConfiguration;
import cz.encircled.jput.model.PerformanceTestRun;
import cz.encircled.jput.unit.PerformanceAnalyzerImpl;
import org.junit.Test;

/**
 * @author Vlad on 21-May-17.
 */
public class WriterTest {

    @Test
    public void testWriter() {
        PerformanceAnalyzerImpl analyzer = new PerformanceAnalyzerImpl();
        String pathToFile = System.getProperty("java.io.tmpdir") + "jput-test.json";
        FileSystemResultWriter writer = new FileSystemResultWriter(pathToFile);
        MethodConfiguration configuration = new MethodConfiguration().setRepeats(3);
        PerformanceTestRun run = analyzer.build(configuration, getClass().getMethods()[0]);
        analyzer.addRun(run, configuration, 100);
        analyzer.addRun(run, configuration, 110);
        analyzer.addRun(run, configuration, 120);

        writer.appendTrendResult(run);
        writer.flush();
    }

}
