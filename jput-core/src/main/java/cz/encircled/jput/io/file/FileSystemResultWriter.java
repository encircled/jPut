package cz.encircled.jput.io.file;

import cz.encircled.jput.io.TrendResultWriter;
import cz.encircled.jput.model.PerformanceTestRun;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * @author Vlad on 21-May-17.
 */
public class FileSystemResultWriter implements TrendResultWriter {

    private final List<PerformanceTestRun> stack = new ArrayList<>();

    private final Object flushMutex = new Object();

    private Path target;

    public FileSystemResultWriter(String pathToFile) {
        this.target = Paths.get(pathToFile);
    }

    @Override
    public void appendTrendResult(PerformanceTestRun run) {
        synchronized (stack) {
            stack.add(run);
        }
    }

    @Override
    public void flush() {
        synchronized (flushMutex) {
            List<String> mapped;
            synchronized (stack) {
                mapped = stack.stream().map(this::toFileFormat).collect(Collectors.toList());
                stack.clear();
            }

            try {
                Files.write(target, mapped, StandardCharsets.UTF_8, APPEND, CREATE);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to append to file", e);
            }
        }
    }

    private String toFileFormat(PerformanceTestRun run) {
        StringBuilder array = new StringBuilder();
        // TODO aggregate method runs?
        for (long l : run.runs) {
            if (array.length() > 0) {
                array.append(",");
            }
            array.append(l);
        }
        return String.join(";", Long.toString(run.executionId), array.toString(), run.testClass + "#" + run.testMethod);
    }

}
