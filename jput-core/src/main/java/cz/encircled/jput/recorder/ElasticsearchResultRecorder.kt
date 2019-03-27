package cz.encircled.jput.recorder

import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.context
import cz.encircled.jput.context.getCollectionProperty
import cz.encircled.jput.context.getProperty
import cz.encircled.jput.model.PerfTestExecution
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.get.GetIndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.Requests
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Elasticsearch-based implementation of tests execution results recorder
 */
class ElasticsearchResultRecorder(private val client: RestHighLevelClient) : ThreadsafeResultRecorder() {

    val log = LoggerFactory.getLogger(ElasticsearchResultRecorder::class.java)

    init {
        createIndexIfNeeded()
    }

    override fun getSample(execution: PerfTestExecution): List<Long> {
        val conf = execution.conf.trendConfiguration!!
        val type = getProperty(JPutContext.PROP_ELASTIC_TYPE, "default")

        val queryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.matchQuery("testId", execution.conf.testId))

        getCollectionProperty(JPutContext.PROP_ELASTIC_ENV_IDENTIFIERS).forEach {
            queryBuilder.filter(QueryBuilders.matchQuery(it, getProperty(it, "")))
        }

        val request = SearchRequest("jput").types(type)
                .source(SearchSourceBuilder().query(queryBuilder))

        val searchResponse = client.search(request, RequestOptions.DEFAULT)
        val sample = subList(searchResponse.hits.hits.toList(), conf.sampleSize, conf.sampleSelectionStrategy)

        return sample
                .map {
                    val runs = it.sourceAsMap["runs"]
                    runs as List<*>
                }
                .flatten()
                .map {
                    if (it is Int) it.toLong()
                    else it as Long
                }
    }

    override fun doFlush(data: List<PerfTestExecution>) {
        log.info("Do flush to Elasticsearch: $data")

        val type = getProperty(JPutContext.PROP_ELASTIC_TYPE, "default")

        data.forEach {
            val jsonMap = mutableMapOf(
                    "executionId" to context.executionId,
                    "testId" to it.conf.testId,
                    "times" to it.executionResult,
                    "@timestamp" to Date()
            )

            jsonMap.putAll(getUserDefinedEnvParams())

            val indexRequest = Requests.indexRequest("jput")
                    .type(type)
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

    private fun createIndexIfNeeded() {
        val exists = client.indices().exists(GetIndexRequest().indices("jput"), RequestOptions.DEFAULT)
        if (!exists) {
            val createRequest = CreateIndexRequest("jput")
            client.indices().create(createRequest, RequestOptions.DEFAULT)
        }

    }


}