package cz.encircled.jput.test

import cz.encircled.jput.context.*
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
    fun testGetProperty() {
        System.setProperty("testGetProperty", "true")

        context = JPutContext()
        assertEquals("true", getProperty("testGetProperty", "def"))
        assertEquals("def", getProperty("testGetProperty2", "def"))

        assertEquals(true, getProperty("testGetProperty", false))
        assertEquals(false, getProperty("testGetProperty2", false))

        System.setProperty("testGetProperty", "true,false")
        assertEquals(listOf("true", "false"), getCollectionProperty("testGetProperty", listOf("true")))
        assertEquals(listOf("true"), getCollectionProperty("testGetProperty2", listOf("true")))
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

}