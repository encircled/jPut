package cz.encircled.jput.recorder

import cz.encircled.jput.model.PerfTestExecution
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption.APPEND
import java.nio.file.StandardOpenOption.CREATE

/**
 * Filesystem-based implementation of tests execution results recorder
 *
 * @author Vlad on 21-May-17.
 */
class FileSystemResultRecorder(pathToFile: String) : ThreadsafeResultRecorder() {

    val target: Path = Paths.get(pathToFile)

    private var runs: MutableMap<String, List<Long>> = mutableMapOf()

    init {
        initRuns()
    }

    // TODO ignore error results
    override fun getSample(execution: PerfTestExecution): List<Long> {
        check(execution.conf.trendConfiguration != null) { "Trend configuration must be defined" }

        val config = execution.conf.trendConfiguration
        val sample = runs.getOrDefault(execution.conf.testId, listOf())

        return subList(sample, config.sampleSize, config.sampleSelectionStrategy)
    }

    override fun doFlush(data: List<PerfTestExecution>) {
        val mapped = data.map(this::toFileFormat)
        Files.write(target, mapped, StandardCharsets.UTF_8, APPEND, CREATE)
    }

    private fun toFileFormat(execution: PerfTestExecution): String {
        val array = execution.getElapsedTimes().joinToString(",")
        return arrayOf(execution.executionParams["id"], array, execution.conf.testId).joinToString(";")
    }

    private fun initRuns() {
        try {
            val file = target.toFile()
            if (!file.exists()) {
                file.createNewFile()
            }

            val strings = Files.readAllLines(target, StandardCharsets.UTF_8)
            strings.forEach { s ->
                val split = s.split(";").dropLastWhile { it.isEmpty() }
                val methodRuns: MutableList<Long> = runs.computeIfAbsent(split[2]) { mutableListOf() } as MutableList<Long>
                val lineRuns = split[1].split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .map { it.toLong() }
                methodRuns.addAll(lineRuns)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
