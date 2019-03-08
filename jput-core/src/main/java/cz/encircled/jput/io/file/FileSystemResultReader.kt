package cz.encircled.jput.io.file

import cz.encircled.jput.io.TrendResultReader
import cz.encircled.jput.model.PerformanceTestExecution
import java.io.IOException
import java.lang.Exception
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.streams.toList

/**
 * @author Vlad on 28-May-17.
 */
class FileSystemResultReader(pathToFile: String) : TrendResultReader {

    private val target: Path = Paths.get(pathToFile)

    private var runs: MutableMap<String, List<Long>> = mutableMapOf()

    init {
        initRuns()
    }

    override fun getReferenceExecutions(newExecution: PerformanceTestExecution, referenceExecutionsCount: Int): List<Long> {
        val key = newExecution.testClass + "#" + newExecution.testMethod
        val referenceExecutions = runs.getOrDefault(key, listOf())

        return if (referenceExecutions.size > referenceExecutionsCount) referenceExecutions.subList(0, referenceExecutionsCount) else referenceExecutions
    }

    private fun initRuns() {
        try {
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
