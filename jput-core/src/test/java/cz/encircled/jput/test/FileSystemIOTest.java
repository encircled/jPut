package cz.encircled.jput.test;

import java.io.File;

import cz.encircled.jput.io.TrendResultReader;
import cz.encircled.jput.io.file.FileSystemResultReader;
import cz.encircled.jput.io.file.FileSystemResultWriter;
import cz.encircled.jput.model.PerformanceTestRun;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vlad on 21-May-17.
 */
public class FileSystemIOTest {

    @Test
    public void testWriter() {
        String pathToFile = System.getProperty("java.io.tmpdir") + "jput-test.data";
        new File(pathToFile).delete();

        FileSystemResultWriter writer = new FileSystemResultWriter(pathToFile);

        PerformanceTestRun run = TestSupport.getRun(getClass().getMethods()[0], 100, 110, 120);

        writer.appendTrendResult(run);
        writer.appendTrendResult(TestSupport.getRun(getClass().getMethods()[0], 130, 115, 105));
        writer.flush();

        TrendResultReader reader = new FileSystemResultReader(pathToFile);
        long[] runs = reader.getStandardSampleRuns(run, 100);
        Assert.assertArrayEquals(new long[]{ 100, 110, 120, 130, 115, 105 }, runs);
        runs = reader.getStandardSampleRuns(run, 4);
        Assert.assertArrayEquals(new long[]{ 100, 110, 120, 130 }, runs);
    }

}
