package cz.encircled.jput.recorder

import cz.encircled.jput.context.context
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

    override fun getSample(execution: PerfTestExecution, sampleSize: Int): List<Long> {
        val sourceBuilder = SearchSourceBuilder()
        sourceBuilder.query(QueryBuilders.matchQuery("testId", execution.testId!!))

        val request = SearchRequest("jput").types("perf")
                .source(sourceBuilder)

        val searchResponse = client.search(request, RequestOptions.DEFAULT)
        return searchResponse.hits.hits
                .take(sampleSize)
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
        data.forEach {
            val jsonMap = mapOf(
                    "executionId" to context.executionId,
                    "testId" to it.testId,
                    "runs" to it.executionResult
            )
            val indexRequest = Requests.indexRequest("jput")
                    .type("perf")
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