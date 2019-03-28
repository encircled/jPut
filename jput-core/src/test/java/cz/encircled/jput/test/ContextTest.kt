package cz.encircled.jput.test

import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.context
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

}