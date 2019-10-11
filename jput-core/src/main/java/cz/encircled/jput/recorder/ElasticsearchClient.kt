package cz.encircled.jput.recorder

import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClientBuilder
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.reindex.BulkByScrollResponse
import org.elasticsearch.index.reindex.DeleteByQueryRequest

/**
 * @author Vlad on 15-Sep-19.
 */
interface ElasticsearchClient {

    fun search(searchRequest: SearchRequest, options: RequestOptions): SearchResponse

    fun bulk(request: BulkRequest, options: RequestOptions): BulkResponse

    fun deleteByQuery(deleteByQueryRequest: DeleteByQueryRequest, options: RequestOptions): BulkByScrollResponse

    fun close()

}

class ElasticsearchClientWrapper(builder: RestClientBuilder) : ElasticsearchClient {

    private val delegate = RestHighLevelClient(builder)

    override fun deleteByQuery(deleteByQueryRequest: DeleteByQueryRequest, options: RequestOptions): BulkByScrollResponse =
            delegate.deleteByQuery(deleteByQueryRequest, options)

    override fun search(searchRequest: SearchRequest, options: RequestOptions): SearchResponse =
            delegate.search(searchRequest, options)

    override fun bulk(request: BulkRequest, options: RequestOptions): BulkResponse =
            delegate.bulk(request, options)

    override fun close() = delegate.close()
}