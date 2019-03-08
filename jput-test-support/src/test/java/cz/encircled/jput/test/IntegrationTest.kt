package cz.encircled.jput.test

import cz.encircled.jput.spring.JPutSpringRunner
import org.elasticsearch.client.node.NodeClient
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.annotations.Setting
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories
import org.springframework.test.context.ContextConfiguration
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.apache.http.HttpHost
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.client.Client
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.TransportAddress
import java.net.InetAddress
import org.elasticsearch.transport.client.PreBuiltTransportClient


/**
 * @author encir on 20-Feb-19.
 */
@ContextConfiguration(classes = [Conf::class])
@RunWith(JPutSpringRunner::class)
class Test {

    // @PerformanceTest(maxTimeLimit = 1000L, performanceTrend = [PerformanceTrend(averageTimeThreshold = 1.0)])
    @Test
    fun test() {
        var client: RestHighLevelClient? = null
        try {
            client = RestHighLevelClient(
                    RestClient.builder(
                            HttpHost("ecs.pif.test.eit.zone", 80, "http")))

            val getRequest = GetRequest(
                    "changelog-logstash-2019.03.08",
                    "flb_type",
                    "igqaWmkBK5rZMZ6ISsne")

            val getResponse = client.get(getRequest, RequestOptions.DEFAULT)
            getResponse.fields
        } finally {
            client!!.close()
        }

    }

}

@Configuration
@ComponentScan(basePackages = ["cz.encircled.jput.test"])
open class Conf {


}