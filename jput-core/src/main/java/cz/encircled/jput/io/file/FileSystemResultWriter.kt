package cz.encircled.jput.io.file

import cz.encircled.jput.io.TrendResultWriter
import cz.encircled.jput.model.PerformanceTestExecution
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption.APPEND
import java.nio.file.StandardOpenOption.CREATE
import java.util.*

/**
 * @author Vlad on 21-May-17.
 */
class FileSystemResultWriter(pathToFile: String) : TrendResultWriter {

    private val stack = ArrayList<PerformanceTestExecution>()

    private val flushMutex = Any()

    private val target: Path = Paths.get(pathToFile)

    override fun appendTrendResult(execution: PerformanceTestExecution) {
        synchronized(stack) {
            stack.add(execution)
        }
    }

    override fun flush() {
        synchronized(flushMutex) {
            var mapped: List<String> = listOf()
            synchronized(stack) {
                mapped = stack.map(this::toFileFormat)
                stack.clear()
            }

            try {
                Files.write(target, mapped, StandardCharsets.UTF_8, APPEND, CREATE)
            } catch (e: IOException) {
                throw IllegalStateException("Failed to append to file", e)
            }

        }
    }

    private fun toFileFormat(execution: PerformanceTestExecution): String {
        // TODO aggregate method runs?
        val array = execution.runs!!.joinToString(",")
        return arrayOf(java.lang.Long.toString(execution.executionId), array, execution.testClass + "#" + execution.testMethod).joinToString(";")
    }

}
