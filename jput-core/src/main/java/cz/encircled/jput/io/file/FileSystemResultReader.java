package cz.encircled.jput.io.file;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cz.encircled.jput.io.TrendResultReader;
import cz.encircled.jput.model.PerformanceTestRun;

/**
 * @author Vlad on 28-May-17.
 */
public class FileSystemResultReader implements TrendResultReader {

    private Path target;

    private Map<String, long[]> runs;

    public FileSystemResultReader(String pathToFile) {
        this.target = Paths.get(pathToFile);
        initRuns();
    }

    @Override
    public long[] getStandardSampleRuns(PerformanceTestRun newRun, int standardSampleSize) {
        String key = newRun.testClass + "#" + newRun.testMethod;
        long[] sampleRuns = runs.get(key);
        if (sampleRuns != null && sampleRuns.length > standardSampleSize) {
            long[] trimmed = new long[standardSampleSize];
            System.arraycopy(sampleRuns, 0, trimmed, 0, standardSampleSize);
            sampleRuns = trimmed;
            runs.put(key, sampleRuns);
        }
        return sampleRuns;
    }

    private void initRuns() {
        Map<String, List<Long>> temp = new HashMap<>();
        try {
            List<String> strings = Files.readAllLines(target, StandardCharsets.UTF_8);
            strings.forEach(s -> {
                String[] split = s.split(";");
                Collection<Long> methodRuns = temp.computeIfAbsent(split[2], k -> new ArrayList<>());
                List<Long> lineRuns = Arrays.stream(split[1].split(",")).map(Long::parseLong).collect(Collectors.toList());
                methodRuns.addAll(lineRuns);
            });
            runs = new HashMap<>();
            for (Map.Entry<String, List<Long>> entry : temp.entrySet()) {
                runs.put(entry.getKey(), entry.getValue().stream().mapToLong(l -> l).toArray());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
