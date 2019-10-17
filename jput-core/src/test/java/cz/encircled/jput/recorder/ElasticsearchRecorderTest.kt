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
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.joda.time.LocalDate
import org.junit.AfterClass
import org.junit.BeforeClass
import kotlin.test.*

/**
 * @author Vlad on 15-Sep-19.
 */
open class ElasticsearchRecorderTest : ShortcutsForTests {

    @BeforeTest
    fun before() {
        wireMockServer.resetAll()
    }

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

        val testIdPredicate = "\"testId\":{\"query\":\"1\",\"operator\":\"AND\""
        val errorPredicate = "\"errorMessage\":{\"query\":\"\",\"operator\":\"AND\""

        val url = RequestPatternBuilder(RequestMethod.POST, UrlPattern(RegexPattern("/jput/_search.*"), true))
        wireMockServer.verify(1, url.withRequestBody(containing(testIdPredicate)))
        wireMockServer.verify(1, url.withRequestBody(containing(errorPredicate)))
    }

    @Test
    fun testGetSampleWithEnvParams() = testWithProps(
            JPutContext.PROP_ENV_PARAMS to "env:localhost,namespace:1",
            JPutContext.PROP_ELASTIC_ENV_IDENTIFIERS to "env,namespace") {
        val client = ElasticsearchClientWrapper(RestClient.builder(HttpHost("localhost", port, "http")))
        val ecs = ElasticsearchResultRecorder(client)

        val execution = getTestExecution(configWithTrend(TrendTestConfiguration(
                sampleSize = 5
        )))
        assertEquals(listOf(95L, 105L), ecs.getSample(execution))

        val predicate: (String, String) -> String = { l, r ->
            "\"match\":{\"$l\":{\"query\":\"$r\",\"operator\":\"AND\""
        }

        val testIdPredicate = predicate("testId", "1")
        val errorPredicate = predicate("errorMessage", "")
        val envPredicate = predicate("env", "localhost")
        val nsPredicate = predicate("namespace", "1")

        val url = RequestPatternBuilder(RequestMethod.POST, UrlPattern(RegexPattern("/jput/_search.*"), true))
        wireMockServer.verify(1, url.withRequestBody(containing(testIdPredicate)))
        wireMockServer.verify(1, url.withRequestBody(containing(errorPredicate)))
        wireMockServer.verify(1, url.withRequestBody(containing(nsPredicate)))
        wireMockServer.verify(1, url.withRequestBody(containing(envPredicate)))
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
                    "{\"executionId\":${context.executionId}," +
                    "\"testId\":\"1\"," +
                    "\"start\":\"1970-01-01T00:00:00.${run.relativeStartTime / 1000000}Z\"," +
                    "\"elapsed\":${run.elapsedTime}," +
                    "\"resultCode\":${run.resultDetails.resultCode}," +
                    "\"errorMessage\":\"${errorMsg}\"," +
                    "\"test1\":\"1\",\"test2\":\"abc\"}\n"
        }

        val expected = builder(successCase, "") + builder(errorAndMessageCase, "TestError. Whoops") +
                builder(onlyMessageCase, "TestError") + builder(allNullCase, "") + builder(noDetailsCase, "")

        wireMockServer.verify(postRequestedFor(urlEqualTo("/_bulk?timeout=1m"))
                .withRequestBody(equalTo(expected)))
    }

    @Test
    fun testAutoCleanup() = testWithProps(
            JPutContext.PROP_ELASTIC_ENABLED to "true",
            JPutContext.PROP_ELASTIC_HOST to "localhost",
            JPutContext.PROP_ELASTIC_PORT to "9200",
            JPutContext.PROP_ELASTIC_CLEANUP_DAYS to "5") {

        context = JPutContext()
        context.init()

        val time = LocalDate.now().minusDays(5).toDate().time
        val expected = "{\"size\":1000,\"query\":{\"range\":{\"executionId\":{\"from\":null,\"to\":${time},\"include_lower\":true,\"include_upper\":false,\"boost\":1.0}}},\"_source\":false}"

        wireMockServer.verify(postRequestedFor(urlMatching("/jput/_delete_by_query.*"))
                .withRequestBody(equalTo(expected)))
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
