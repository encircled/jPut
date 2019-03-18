package cz.encircled.jput.recorder

import cz.encircled.jput.context.JPutContext
import cz.encircled.jput.context.context
import cz.encircled.jput.context.getProperty
import cz.encircled.jput.model.MethodTrendConfiguration
import cz.encircled.jput.model.PerfTestExecution
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.get.GetIndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.Requests
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder

class ElasticsearchResultRecorder(private val client: RestHighLevelClient) : ThreadsafeResultRecorder() {

    init {
        createIndexIfNeeded()
    }

    override fun getSample(execution: PerfTestExecution, config: MethodTrendConfiguration): List<Long> {
        val sourceBuilder = SearchSourceBuilder()
        sourceBuilder.query(QueryBuilders.matchQuery("testId", execution.testId!!))

        val request = SearchRequest("jput").types("perf")
                .source(sourceBuilder)

        val searchResponse = client.search(request, RequestOptions.DEFAULT)
        val sample = subList(searchResponse.hits.hits.toList(), config.sampleSize, config.sampleSelectionStrategy)

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
        val type = getProperty(JPutContext.PROP_ELASTIC_TYPE, "default")

        data.forEach {
            val jsonMap = mutableMapOf(
                    "executionId" to context.executionId,
                    "testId" to it.testId,
                    "runs" to it.executionResult
            )

            jsonMap.putAll(getUserDefinedEnvParams())

            val indexRequest = Requests.indexRequest("jput")
                    .type(type)
                    .source(jsonMap)

            client.index(indexRequest, RequestOptions.DEFAULT)
        }


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