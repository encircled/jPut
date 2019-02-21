package cz.encircled.jput.test

import cz.encircled.jput.io.file.FileSystemResultReader
import cz.encircled.jput.io.file.FileSystemResultWriter
import org.junit.Assert
import org.junit.Test
import java.io.File

/**
 * @author Vlad on 21-May-17.
 */
class FileSystemIOTest {

    @Test
    fun testWriter() {
        val pathToFile = System.getProperty("java.io.tmpdir") + "jput-test.data"
        File(pathToFile).delete()

        val writer = FileSystemResultWriter(pathToFile)

        val run = TestSupport.getTestExecution(javaClass.methods[0], 100, 110, 120)

        writer.appendTrendResult(run)
        writer.appendTrendResult(TestSupport.getTestExecution(javaClass.methods[0], 130, 115, 105))
        writer.flush()

        val reader = FileSystemResultReader(pathToFile)
        var runs = reader.getStandardSampleRuns(run, 100)
        Assert.assertArrayEquals(longArrayOf(100, 110, 120, 130, 115, 105), runs)
        runs = reader.getStandardSampleRuns(run, 4)
        Assert.assertArrayEquals(longArrayOf(100, 110, 120, 130), runs)
    }

}
