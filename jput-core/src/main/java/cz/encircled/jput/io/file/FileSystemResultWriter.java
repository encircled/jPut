package cz.encircled.jput.io.file;

import com.google.gson.Gson;
import cz.encircled.jput.io.TrendResultWriter;
import cz.encircled.jput.model.PerformanceTestRun;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * @author Vlad on 21-May-17.
 */
public class FileSystemResultWriter implements TrendResultWriter {

    private final List<PerformanceTestRun> stack = new ArrayList<>();

    private Gson gson = new Gson();

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
        synchronized (stack) {
            String json = gson.toJson(stack);
            stack.clear();

            try {
                Files.write(target, Collections.singletonList(json), StandardCharsets.UTF_8, APPEND, CREATE);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to append to file", e);
            }
        }
    }

}
