package cz.encircled.jput.test

import cz.encircled.jput.JPutJUnitRunner
import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.model.MethodTrendConfiguration
import cz.encircled.jput.recorder.FileSystemResultRecorder
import cz.encircled.jput.trend.SelectionStrategy
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Vlad on 21-May-17.
 */
@RunWith(JPutJUnitRunner::class)
class FileSystemRecorderTest {

    @Test
    fun testWriter() {
        val (pathToFile, writer) = getWriter()

        val run = TestSupport.getTestExecution(javaClass.methods[0], 100, 110, 120)

        writer.appendTrendResult(run)
        writer.appendTrendResult(TestSupport.getTestExecution(javaClass.methods[0], 130, 115, 105))
        writer.flush()

        val config = MethodTrendConfiguration(100, sampleSelectionStrategy = SelectionStrategy.USE_FIRST)

        val reader = FileSystemResultRecorder(pathToFile)
        var runs = reader.getSample(run, config)
        assertEquals(listOf<Long>(100, 110, 120, 130, 115, 105), runs)

        val config2 = MethodTrendConfiguration(4, sampleSelectionStrategy = SelectionStrategy.USE_FIRST)
        runs = reader.getSample(run, config2)
        assertEquals(listOf<Long>(100, 110, 120, 130), runs)
    }

    @Test
    fun testSampleStrategy() {
        val (_, writer) = getWriter()

        assertEquals(listOf(1, 2), writer.subList(listOf(1, 2, 3, 4), 2, SelectionStrategy.USE_FIRST))
        assertEquals(listOf(3, 4), writer.subList(listOf(1, 2, 3, 4), 2, SelectionStrategy.USE_LATEST))
    }

    @Test
    fun testUserDefinedEnvParams() {
        System.setProperty(JPutContext.PROP_ENV_PARAMS, "test1:1,test2:abc")
        val (_, writer) = getWriter()

        assertEquals(mapOf(
                "test1" to "1",
                "test2" to "abc"
        ), writer.getUserDefinedEnvParams())
    }

    private fun getWriter(): Pair<String, FileSystemResultRecorder> {
        val pathToFile = System.getProperty("java.io.tmpdir") + "jput-test.data"
        File(pathToFile).delete()

        val writer = FileSystemResultRecorder(pathToFile)
        return Pair(pathToFile, writer)
    }

}
