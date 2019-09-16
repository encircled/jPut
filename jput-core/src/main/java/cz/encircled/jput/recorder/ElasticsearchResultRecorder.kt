package cz.encircled.jput.recorder

import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.context
import cz.encircled.jput.context.getCollectionProperty
import cz.encircled.jput.context.getProperty
import cz.encircled.jput.model.PerfTestExecution
import org.elasticsearch.ElasticsearchStatusException
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.Requests
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Elasticsearch-based implementation of tests execution results recorder
 */
class ElasticsearchResultRecorder(private val client: ElasticsearchClient) : ThreadsafeResultRecorder() {

    private val log = LoggerFactory.getLogger(ElasticsearchResultRecorder::class.java)

    private val indexName by lazy { getProperty(JPutContext.PROP_ELASTIC_INDEX, "jput") }

    override fun getSample(execution: PerfTestExecution): List<Long> {
        val conf = execution.conf.trendConfiguration!!

        val queryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.matchQuery("testId", execution.conf.testId))

        getCollectionProperty(JPutContext.PROP_ELASTIC_ENV_IDENTIFIERS).forEach {
            queryBuilder.filter(QueryBuilders.matchQuery(it, getProperty(it, "")))
        }

        val request = SearchRequest(indexName).source(SearchSourceBuilder().query(queryBuilder))

        val searchResponse = try {
            client.search(request, RequestOptions.DEFAULT)
        } catch (e: ElasticsearchStatusException) {
            if (e.status() == RestStatus.NOT_FOUND) {
                return emptyList()
            }
            throw e
        }
        val sample = subList(searchResponse.hits.hits.toList(), conf.sampleSize, conf.sampleSelectionStrategy)

        return sample
                .map {
                    val times = it.sourceAsMap["times"]
                    times as List<*>
                }
                .flatten()
                .map {
                    if (it is Int) it.toLong()
                    else it as Long
                }
    }

    override fun doFlush(data: List<PerfTestExecution>) {
        log.info("Do flush to Elasticsearch: $data")

        data.forEach {
            val jsonMap = mutableMapOf(
                    "executionId" to context.executionId,
                    "testId" to it.conf.testId,
                    "times" to it.executionResult,
                    "@timestamp" to Date()
            )

            jsonMap.putAll(getUserDefinedEnvParams())

            val indexRequest = Requests.indexRequest(indexName)
                    .type("jput")
                    .source(jsonMap)

            client.index(indexRequest, RequestOptions.DEFAULT)
        }

        log.info("Flush to Elasticsearch done")
    }

    override fun destroy() {
        try {
            client.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}