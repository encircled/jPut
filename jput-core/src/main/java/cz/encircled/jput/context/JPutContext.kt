package cz.encircled.jput.context

import cz.encircled.jput.recorder.ElasticsearchResultRecorder
import cz.encircled.jput.recorder.FileSystemResultRecorder
import cz.encircled.jput.recorder.ResultRecorder
import cz.encircled.jput.trend.SampleBasedTrendAnalyzer
import cz.encircled.jput.trend.TrendAnalyzer
import cz.encircled.jput.unit.UnitPerformanceAnalyzer
import cz.encircled.jput.unit.UnitPerformanceAnalyzerImpl
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.slf4j.LoggerFactory

lateinit var context: JPutContext

/**
 *
 * @author Vlad on 21-May-17.
 */
class JPutContext {
    /**
     * Unique ID of current tests execution
     */
    var executionId: Long = 0

    /**
     * Global enabled/disabled switch
     */
    var isPerformanceTestEnabled = true

    var propertySources: List<PropertySource> = listOf(SystemPropertySource())
    lateinit var unitPerformanceAnalyzer: UnitPerformanceAnalyzer
    lateinit var trendAnalyzer: TrendAnalyzer

    var resultRecorders: List<ResultRecorder> = listOf()

    val log = LoggerFactory.getLogger(JPutContext::class.java)

    fun init() {
        isPerformanceTestEnabled = getProperty(PROP_ENABLED, true)
        executionId = System.currentTimeMillis()
        unitPerformanceAnalyzer = UnitPerformanceAnalyzerImpl()
        trendAnalyzer = SampleBasedTrendAnalyzer()

        initRecorders()
    }

    private fun initRecorders() {
        if (getProperty(PROP_ELASTIC_ENABLED, false)) {
            val host = getProperty<String>(PROP_ELASTIC_HOST)
            val port = getProperty(PROP_ELASTIC_PORT, 80)
            val scheme = getProperty(PROP_ELASTIC_SCHEME, "http")

            log.info("JPut using Elasticsearch $host")

            val client = RestHighLevelClient(RestClient.builder(HttpHost(host, port, scheme)))
            addResultRecorder(ElasticsearchResultRecorder(client))
        }

        if (getProperty(PROP_STORAGE_FILE_ENABLED, false)) {

            val default = System.getProperty("java.recorder.tmpdir") + "jput-test.data"
            val pathToFile = getProperty(PROP_PATH_TO_STORAGE_FILE, default)

            log.info("JPut using Filesystem $pathToFile")
            addResultRecorder(FileSystemResultRecorder(pathToFile))
        }
    }

    fun destroy() {
        resultRecorders.forEach {
            it.flush()
            it.destroy()
        }
    }

    fun addPropertySource(source: PropertySource) {
        propertySources = listOf(SystemPropertySource(), source)
    }

    private fun addResultRecorder(resultRecorder: ResultRecorder) {
        val temp = ArrayList(resultRecorders)
        temp.add(resultRecorder)
        resultRecorders = temp
    }

    companion object {

        private const val PREFIX = "jput."

        const val PROP_ENABLED = PREFIX + "enabled"

        const val PROP_ELASTIC_ENABLED = PREFIX + "storage.elastic.enabled"

        const val PROP_ELASTIC_HOST = PREFIX + "storage.elastic.host"

        const val PROP_ELASTIC_PORT = PREFIX + "storage.elastic.port"

        const val PROP_ELASTIC_SCHEME = PREFIX + "storage.elastic.scheme"

        const val PROP_ELASTIC_TYPE = PREFIX + "storage.elastic.type"

        const val PROP_ELASTIC_ENV_IDENTIFIERS = PREFIX + "storage.elastic.env.identifiers"

        const val PROP_STORAGE_FILE_ENABLED = PREFIX + "storage.file.enabled"

        const val PROP_PATH_TO_STORAGE_FILE = PREFIX + "storage.file.path"

        const val PROP_ENV_PARAMS = PREFIX + "env.custom.params"

    }

}
