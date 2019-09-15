package cz.encircled.jput.recorder

import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClientBuilder
import org.elasticsearch.client.RestHighLevelClient

/**
 * @author Vlad on 15-Sep-19.
 */
interface ElasticsearchClient {

    fun search(searchRequest: SearchRequest, options: RequestOptions): SearchResponse

    fun index(indexRequest: IndexRequest, options: RequestOptions): IndexResponse

    fun close()

}

class ElasticsearchClientWrapper(builder: RestClientBuilder) : ElasticsearchClient {

    private val delegate = RestHighLevelClient(builder)

    override fun search(searchRequest: SearchRequest, options: RequestOptions): SearchResponse =
            delegate.search(searchRequest, options)

    override fun index(indexRequest: IndexRequest, options: RequestOptions): IndexResponse =
            delegate.index(indexRequest, options)

    override fun close() = delegate.close()
}