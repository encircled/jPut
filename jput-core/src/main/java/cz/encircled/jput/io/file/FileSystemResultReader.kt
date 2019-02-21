package cz.encircled.jput.io.file

import cz.encircled.jput.io.TrendResultReader
import cz.encircled.jput.model.PerformanceTestExecution
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 * @author Vlad on 28-May-17.
 */
class FileSystemResultReader(pathToFile: String) : TrendResultReader {

    private val target: Path = Paths.get(pathToFile)

    private var runs: MutableMap<String, LongArray>? = null

    init {
        initRuns()
    }

    override fun getStandardSampleRuns(newExecution: PerformanceTestExecution, standardSampleSize: Int): LongArray? {
        val key = newExecution.testClass + "#" + newExecution.testMethod
        var sampleRuns: LongArray? = runs!![key] ?: return null

        if (sampleRuns!!.size > standardSampleSize) {
            val trimmed = LongArray(standardSampleSize)
            System.arraycopy(sampleRuns, 0, trimmed, 0, standardSampleSize)
            sampleRuns = trimmed
            runs!![key] = sampleRuns
        }

        return sampleRuns
    }

    private fun initRuns() {
        val temp = HashMap<String, List<Long>>()
        try {
            val strings = Files.readAllLines(target, StandardCharsets.UTF_8)
            strings.forEach { s ->
                val split = s.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val methodRuns: MutableList<Long> = temp.computeIfAbsent(split[2]) { mutableListOf() } as MutableList<Long>
                val lineRuns = split[1].split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .map { it.toLong() }
                methodRuns.addAll(lineRuns)
            }
            runs = HashMap()
            for ((key, value) in temp) {
                runs!![key] = value.stream().mapToLong { l -> l }.toArray()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

}
