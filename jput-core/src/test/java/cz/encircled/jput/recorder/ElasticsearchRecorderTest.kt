package cz.encircled.jput.recorder

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.matching.RegexPattern
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import com.github.tomakehurst.wiremock.matching.UrlPattern
import cz.encircled.jput.ShortcutsForTests
import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.context
import cz.encircled.jput.model.ExecutionRun
import cz.encircled.jput.model.RunResult
import cz.encircled.jput.model.TrendTestConfiguration
import cz.encircled.jput.runner.JPutJUnit4Runner
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Vlad on 15-Sep-19.
 */
@RunWith(JPutJUnit4Runner::class)
open class ElasticsearchRecorderTest : ShortcutsForTests {

    @AfterTest
    fun after() {
        wireMockServer.resetRequests()
    }

    @Test
    fun testGetSample() {
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
        val client = ElasticsearchClientWrapper(RestClient.builder(HttpHost("localhost", port, "http")))
        val ecs = ElasticsearchResultRecorder(client)

        val execution = getTestExecution(configWithTrend(TrendTestConfiguration(
                sampleSize = 5
        )))
        assertTrue(ecs.getSample(execution).isEmpty())

        wireMockServer.verify(1, RequestPatternBuilder(RequestMethod.POST, UrlPattern(RegexPattern("/new/_search.*"), true)))
    }

    @Test
    fun testAddingEntries() = testWithProps(JPutContext.PROP_ENV_PARAMS to "test1:1,test2:abc") {
        val client = ElasticsearchClientWrapper(RestClient.builder(HttpHost("localhost", port, "http")))
        val ecs = ElasticsearchResultRecorder(client)

        val execution = getTestExecution(configWithTrend(TrendTestConfiguration(
                sampleSize = 5
        )))

        val successCase = ExecutionRun(execution, 321000000L, 321L)
        successCase.resultDetails = RunResult(200)

        val errorAndMessageCase = ExecutionRun(execution, 123000000L, 4321L)
        errorAndMessageCase.resultDetails = RunResult(503, RuntimeException("Whoops"), "TestError")

        val onlyMessageCase = ExecutionRun(execution, 123000000L, 4321L)
        onlyMessageCase.resultDetails = RunResult(503, errorMessage = "TestError")

        val allNullCase = ExecutionRun(execution, 123000000L, 4321L)
        allNullCase.resultDetails = RunResult()

        val noDetailsCase = ExecutionRun(execution, 123000000L, 4321L)

        execution.executionResult[1] = successCase
        execution.executionResult[2] = errorAndMessageCase
        execution.executionResult[3] = onlyMessageCase
        execution.executionResult[4] = allNullCase
        execution.executionResult[5] = noDetailsCase

        ecs.appendTrendResult(execution)
        ecs.flush()

        val builder = { run: ExecutionRun, errorMsg: String ->
            "{\"index\":" +
                    "{\"_index\":\"jput\",\"_type\":\"jput\"}}\n" +
                    "{\"executionId\":\"${context.executionId}\"," +
                    "\"testId\":\"1\"," +
                    "\"start\":\"1970-01-01T00:00:00.${run.relativeStartTime / 1000000}Z\"," +
                    "\"elapsed\":${run.elapsedTime}," +
                    "\"resultCode\":${run.resultDetails?.resultCode}," +
                    "\"errorMessage\":\"${errorMsg}\"," +
                    "\"test1\":\"1\",\"test2\":\"abc\"}\n"
        }

        val expected = builder(successCase, "") + builder(errorAndMessageCase, "TestError. Whoops") +
                builder(onlyMessageCase, "TestError") + builder(allNullCase, "") + builder(noDetailsCase, "")

        wireMockServer.verify(postRequestedFor(urlEqualTo("/_bulk?timeout=1m"))
                .withRequestBody(equalTo(expected)))
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
            configureFor(port)
        }

        @AfterClass
        @JvmStatic
        fun afterClass() {
            wireMockServer.stop()
        }

    }

}
