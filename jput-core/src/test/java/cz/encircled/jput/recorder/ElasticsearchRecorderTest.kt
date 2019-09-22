package cz.encircled.jput.recorder

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.matching.RegexPattern
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import com.github.tomakehurst.wiremock.matching.UrlPattern
import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.context
import cz.encircled.jput.model.TrendTestConfiguration
import cz.encircled.jput.recorder.ElasticsearchClientWrapper
import cz.encircled.jput.recorder.ElasticsearchResultRecorder
import cz.encircled.jput.ShortcutsForTests
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.junit.AfterClass
import org.junit.BeforeClass
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Vlad on 15-Sep-19.
 */
open class ElasticsearchRecorderTest : ShortcutsForTests {

    @AfterTest
    fun after() {
        wireMockServer.resetRequests()
    }

    @Test
    fun testGetSample() {
        context = JPutContext()
        context.init()

        val client = ElasticsearchClientWrapper(RestClient.builder(HttpHost("localhost", port, "http")))
        val ecs = ElasticsearchResultRecorder(client)

        val execution = getTestExecution(configWithTrend(TrendTestConfiguration(
                sampleSize = 5
        )))
        assertEquals(listOf(95L, 105L), ecs.getSample(execution))

        wireMockServer.verify(1, RequestPatternBuilder(RequestMethod.POST, UrlPattern(RegexPattern("/jput/_search.*"), true)))
    }

    @Test
    fun testGetSampleWhenIndexNotExist() = testWithProps(JPutContext.PROP_ELASTIC_INDEX to "new") {
        context = JPutContext()
        context.init()

        val client = ElasticsearchClientWrapper(RestClient.builder(HttpHost("localhost", port, "http")))
        val ecs = ElasticsearchResultRecorder(client)

        val execution = getTestExecution(configWithTrend(TrendTestConfiguration(
                sampleSize = 5
        )))
        assertTrue(ecs.getSample(execution).isEmpty())

        wireMockServer.verify(1, RequestPatternBuilder(RequestMethod.POST, UrlPattern(RegexPattern("/new/_search.*"), true)))
    }

    @Test
    fun testAddingEntries() {
        context = JPutContext()
        context.init()

        val client = ElasticsearchClientWrapper(RestClient.builder(HttpHost("localhost", port, "http")))
        val ecs = ElasticsearchResultRecorder(client)

        val execution = getTestExecution(configWithTrend(TrendTestConfiguration(
                sampleSize = 5
        )))

        execution.startNextExecution()
        execution.finishExecution()
        ecs.appendTrendResult(execution)
        ecs.flush()

        wireMockServer.verify(1, RequestPatternBuilder(RequestMethod.POST, UrlPattern(RegexPattern("/jput/jput.*"), true)))
    }

    @Test
    fun testDestroy() {
        val client = ElasticsearchClientWrapper(RestClient.builder(HttpHost("not_exist", port, "http")))
        val ecs = ElasticsearchResultRecorder(client)
        // Should not fail
        ecs.destroy()
    }

    companion object {

        private const val port = 9200

        private var wireMockServer = WireMockServer(WireMockConfiguration()
                .extensions(ResponseTemplateTransformer(true))
                .port(port))

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            wireMockServer.start()
            WireMock.configureFor(port)
        }

        @AfterClass
        @JvmStatic
        fun afterClass() {
            wireMockServer.stop()
        }

    }

}