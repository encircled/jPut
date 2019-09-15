package cz.encircled.jput.test.recorder

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import com.github.tomakehurst.wiremock.junit.WireMockRule
import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.context
import cz.encircled.jput.model.TrendTestConfiguration
import cz.encircled.jput.recorder.ElasticsearchClientWrapper
import cz.encircled.jput.recorder.ElasticsearchResultRecorder
import cz.encircled.jput.test.ShortcutsForTests
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Vlad on 15-Sep-19.
 */
public open class ElasticsearchRecorderTest : ShortcutsForTests {

    //    @Rule @JvmField
    public val wireMockRule: WireMockRule = WireMockRule(9201);

    private val port = 9201

    var wireMockServer = WireMockServer(WireMockConfiguration()
            .extensions(ResponseTemplateTransformer(true))
            .port(port))

    init {
        wireMockServer.start()

        WireMock.configureFor(port)
    }

    @AfterTest
    fun after() {
        wireMockServer.resetRequests()
        val misses = wireMockServer.findNearMissesForUnmatchedRequests()
        println(misses)
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
    }

}
