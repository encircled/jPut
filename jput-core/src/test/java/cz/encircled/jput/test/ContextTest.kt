package cz.encircled.jput.test

import cz.encircled.jput.context.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContextTest {

    @Test
    fun testDefaultExecutionId() {
        context = JPutContext()
        assertTrue(context.executionId.matches(Regex("\\d\\d\\.\\d\\d\\.\\d\\d \\d\\d:\\d\\d")))
    }

    @Test
    fun testCustomExecutionId() {
        val date = SimpleDateFormat("dd-MM-yyyy").format(Date())
        System.setProperty(JPutContext.PROP_EXECUTION_ID_FORMAT, "dd-MM-yyyy, my format")
        context = JPutContext()
        assertEquals("$date my format", context.executionId)
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

}