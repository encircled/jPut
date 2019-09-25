package cz.encircled.jput

import cz.encircled.jput.context.*
import cz.encircled.jput.model.ExecutionRepeat
import cz.encircled.jput.model.PerfConstraintViolation
import cz.encircled.jput.recorder.ElasticsearchResultRecorder
import cz.encircled.jput.recorder.FileSystemResultRecorder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContextTest : ShortcutsForTests {

    @Test
    fun testDefaultExecutionId() {
        context = JPutContext()
        assertTrue(context.executionId.matches(Regex("[\\d]{13}")))
    }

    @Test
    fun testGetProperty() = testWithProps("getProp" to "true", "collectionProp" to "1,2") {
        context = JPutContext()
        assertEquals("true", getProperty("getProp", "def"))
        assertEquals("def", getProperty("getProp2", "def"))

        assertEquals(true, getProperty("getProp", false))
        assertEquals(false, getProperty("getProp2", false))

        assertEquals(listOf("1", "2"), getCollectionProperty("collectionProp", listOf("true")))
        assertEquals(listOf("true"), getCollectionProperty("collectionProp2", listOf("true")))
    }

    @Test(expected = IllegalStateException::class)
    fun testMissingProperty() {
        context = JPutContext()

        getProperty<String>("doeNotExist")
    }

    @Test
    fun testCustomPropertySource() {
        context = JPutContext()
        val testKey = "not_exists"

        assertEquals("", getProperty(testKey, ""))

        context.addPropertySource(object : PropertySource {
            override fun getProperty(key: String): String? {
                return if (key == testKey) "custom" else null
            }
        })
        assertEquals("custom", getProperty(testKey, ""))
    }

    @Test
    fun testClasspathPropertySource() {
        assertTrue(ClasspathFilePropertySource().getProperty(JPutContext.PROP_REPORTER_CLASS)!!.isNotBlank())
    }

    @Test
    fun testFileSystemRecorderInitialized() {
        val path = System.getProperty("java.io.tmpdir") + "test.test"

        testWithProps(JPutContext.PROP_STORAGE_FILE_ENABLED to "true",
                JPutContext.PROP_PATH_TO_STORAGE_FILE to path) {

            context = JPutContext()
            context.init()
            assertEquals(1, context.resultRecorders.size)
            val recorder = context.resultRecorders[0]
            assertTrue(recorder is FileSystemResultRecorder)
            assertEquals(path, recorder.target.toString())
        }
    }

    @Test
    fun testElasticsearchRecorderInitialized() {
        testWithProps(JPutContext.PROP_ELASTIC_ENABLED to "true",
                JPutContext.PROP_ELASTIC_HOST to "true") {

            context = JPutContext()
            context.init()
            assertEquals(1, context.resultRecorders.size)
            val recorder = context.resultRecorders[0]
            assertTrue(recorder is ElasticsearchResultRecorder)
        }
    }

    @Test
    fun testViolationsErrorMessage() {
        context = JPutContext()
        context.init()

        val execution = getTestExecution(baseConfig())
        execution.executionResult[1] = ExecutionRepeat(execution, 0L, 500L)
        assertEquals(listOf(), execution.violationsErrorMessage)

        execution.executionParams["avgLimit"] = 300L

        execution.violations.addAll(listOf(PerfConstraintViolation.UNIT_MAX, PerfConstraintViolation.UNIT_AVG, PerfConstraintViolation.TREND_AVG))
        assertEquals(listOf(
                "Limit max time = 300 ms, actual max time = 500 ms",
                "Limit avg time = 300 ms, actual avg time = 500 ms",
                "Limit avg time = 300 ms, actual avg time = 500 ms"
        ), execution.violationsErrorMessage)
    }

}