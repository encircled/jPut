package cz.encircled.jput.recorder

import cz.encircled.jput.JPutUtils
import cz.encircled.jput.context.*
import cz.encircled.jput.model.PerfTestExecution
import org.elasticsearch.ElasticsearchStatusException
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.Requests
import org.elasticsearch.index.query.Operator
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.RangeQueryBuilder
import org.elasticsearch.index.reindex.DeleteByQueryRequest
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.slf4j.LoggerFactory


/**
 * Elasticsearch-based implementation of tests execution results recorder
 */
class ElasticsearchResultRecorder(private val client: ElasticsearchClient) : ThreadsafeResultRecorder() {

    private val log = LoggerFactory.getLogger(ElasticsearchResultRecorder::class.java)

    private val indexName by lazy { getProperty(JPutContext.PROP_ELASTIC_INDEX, "jput") }

    override fun getSample(execution: PerfTestExecution): List<Long> {
        val conf = execution.conf.trendConfiguration!!

        val queryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.matchQuery("testId", execution.conf.testId).operator(Operator.AND))

        getCollectionProperty(JPutContext.PROP_ELASTIC_ENV_IDENTIFIERS).forEach {
            queryBuilder.filter(QueryBuilders.matchQuery(it, getProperty(it, "")).operator(Operator.AND))
        }

        queryBuilder.filter(QueryBuilders.matchQuery("errorMessage", "").operator(Operator.AND))

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
                .map { it.sourceAsMap["elapsed"] }
                .map {
                    if (it is Int) it.toLong()
                    else it as Long
                }
    }

    override fun doFlush(data: List<PerfTestExecution>) {
        log.info("Flush to Elasticsearch: ${data.size} test results")

        val bulk = Requests.bulkRequest()
        val userDefined = getUserDefinedEnvParams()

        data.flatMap(::convertToECSDocument).forEach {
            it.putAll(userDefined)

            bulk.add(Requests.indexRequest(indexName)
                    .type("jput")
                    .source(it))
        }

        client.bulk(bulk, RequestOptions.DEFAULT)

        log.info("Successfully flushed to Elasticsearch")
    }

    fun doCleanup() {
        val days = getOptionalProperty<Int>(JPutContext.PROP_ELASTIC_CLEANUP_DAYS)
        if (days != null) {
            log.info("Removing execution older than $days days from Elasticsearch...")
            try {
                val deleted = deleteExecutionsOlderThan(days)
                log.info("Successfully removed $deleted old entries from Elasticsearch!")
            } catch (e: Exception) {
                log.info("Error during Elasticsearch cleanup", e)
            }
        }
    }

    private fun deleteExecutionsOlderThan(days: Int): Long {
        val request = DeleteByQueryRequest(indexName)

        request.setQuery(RangeQueryBuilder("executionId")
                .lt(LocalDate.now().minusDays(days).toDate().time))

        return client.deleteByQuery(request, RequestOptions.DEFAULT).deleted
    }

    private fun convertToECSDocument(it: PerfTestExecution): List<MutableMap<String, Any?>> {
        return it.executionResult.values.map { repeat ->
            mutableMapOf(
                    "executionId" to context.executionId,
                    "testId" to it.conf.testId,
                    "start" to DateTime(repeat.relativeStartTime / 1000000L, DateTimeZone.UTC).toDate(),
                    "elapsed" to repeat.elapsedTime,
                    "resultCode" to repeat.resultDetails.resultCode,
                    "errorMessage" to JPutUtils.buildErrorMessage(repeat)
            )
        }
    }

    override fun destroy() {
        try {
            client.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}