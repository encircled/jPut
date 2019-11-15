package cz.encircled.jput

import cz.encircled.jput.context.*
import cz.encircled.jput.model.ExecutionRun
import cz.encircled.jput.model.PerfConstraintViolation
import cz.encircled.jput.recorder.ElasticsearchResultRecorder
import cz.encircled.jput.recorder.FileSystemResultRecorder
import org.joda.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ContextTest : ShortcutsForTests {

    @Test
    fun testDefaultExecutionId() {
        assertTrue(context.executionId > LocalDate.now().minusDays(1).toDate().time)
    }

    @Test
    fun testGetProperty() = testWithProps(
            "getProp" to "true",
            "mapProp" to "100 = 200, 200=300,300=400",
            "numProp" to "22",
            "collectionProp" to "1,2") {
        context.init()
        assertEquals("true", getProperty("getProp", "def"))
        assertEquals("def", getProperty("getProp2", "def"))

        assertEquals(22L, getProperty("numProp"))
        assertEquals(22L, getProperty("numProp", 23L))
        assertEquals(23L, getProperty("numProp2", 23L))

        assertEquals(22, getProperty("numProp"))

        assertEquals(true, getProperty("getProp", false))
        assertEquals(false, getProperty("getProp2", false))

        assertEquals(listOf("1", "2"), getCollectionProperty("collectionProp", listOf("true")))
        assertEquals(listOf("true"), getCollectionProperty("collectionProp2", listOf("true")))

        assertEquals(mapOf(1.0 to 200L, 2.0 to 300L, 3.0 to 400L), getOptionalMapProperty("mapProp"))
        assertNull(getOptionalMapProperty("mapProp2"))
    }

    @Test(expected = IllegalStateException::class)
    fun testMissingProperty() {
        context = JPutContext()

        getProperty<String>("doeNotExist")
    }

    @Test
    fun testMissingOptionalProperty() {
        context = JPutContext()

        assertNull(getOptionalProperty<String>("doeNotExist"))
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

            context = JPutContext().init()

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
        execution.executionResult[1] = ExecutionRun(execution, 0L, 500L)
        assertEquals(listOf(), execution.violationsErrorMessage)

        execution.executionParams["avgLimit"] = 300L
        execution.executionParams["percentileRank"] = 50
        execution.executionParams["percentileLimit"] = 300
        execution.executionParams["percentileActual"] = 500

        execution.violations.addAll(listOf(PerfConstraintViolation.UNIT_PERCENTILE, PerfConstraintViolation.UNIT_MAX,
                PerfConstraintViolation.UNIT_AVG, PerfConstraintViolation.TREND_AVG))
        assertEquals(listOf(
                "Limit 50 percentile time = 300 ms, actual percentile time = 500 ms",
                "Limit max time = 300 ms, actual max time = 500 ms",
                "Limit avg time = 300 ms, actual avg time = 500 ms",
                "Limit avg time = 300 ms, actual avg time = 500 ms"
        ), execution.violationsErrorMessage)
    }

}