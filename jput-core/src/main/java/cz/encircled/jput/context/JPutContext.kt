package cz.encircled.jput.context

import cz.encircled.jput.JPutReporter
import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.recorder.ElasticsearchClientWrapper
import cz.encircled.jput.recorder.ElasticsearchResultRecorder
import cz.encircled.jput.recorder.FileSystemResultRecorder
import cz.encircled.jput.recorder.ResultRecorder
import cz.encircled.jput.runner.Junit4TestExecutor
import cz.encircled.jput.trend.SampleBasedTrendAnalyzer
import cz.encircled.jput.trend.TrendAnalyzer
import cz.encircled.jput.unit.UnitPerformanceAnalyzer
import cz.encircled.jput.unit.UnitPerformanceAnalyzerImpl
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

lateinit var context: JPutContext

/**
 *
 * @author Vlad on 21-May-17.
 */
class JPutContext {

    private val log = LoggerFactory.getLogger(JPutContext::class.java)

    /**
     * Unique ID of current tests execution
     */
    val executionId: String by lazy {
        System.currentTimeMillis().toString()
    }

    /**
     * Global enabled/disabled switch
     */
    var isPerformanceTestEnabled = true

    var propertySources = mutableListOf(SystemPropertySource(), ClasspathFilePropertySource())

    /**
     * Test results recorders
     */
    val resultRecorders = mutableListOf<ResultRecorder>()

    /**
     * customTestId -> defaultTestId
     */
    val customTestIds = mutableMapOf<String, String>()

    val testExecutions: MutableMap<String, PerfTestExecution> = ConcurrentHashMap()

    lateinit var unitPerformanceAnalyzer: UnitPerformanceAnalyzer
    lateinit var trendAnalyzer: TrendAnalyzer
    lateinit var junit4TestExecutor: Junit4TestExecutor
    var resultReporter: JPutReporter? = null

    fun init() {
        isPerformanceTestEnabled = getProperty(PROP_ENABLED, true)
        unitPerformanceAnalyzer = UnitPerformanceAnalyzerImpl()
        trendAnalyzer = SampleBasedTrendAnalyzer()
        junit4TestExecutor = Junit4TestExecutor()

        resultReporter = getProperty(PROP_REPORTER_CLASS, "").let {
            if (it.isEmpty()) null
            else Class.forName(it).getConstructor().newInstance() as JPutReporter
        }

        initECSRecorder()
        initFileSystemRecorder()
    }

    private fun initFileSystemRecorder() {
        if (getProperty(PROP_STORAGE_FILE_ENABLED, false)) {

            val default = System.getProperty("java.io.tmpdir") + "jput-test.data"
            val pathToFile = getProperty(PROP_PATH_TO_STORAGE_FILE, default)

            log.info("JPut is using Filesystem recorder: $pathToFile")
            resultRecorders.add(FileSystemResultRecorder(pathToFile))
        }
    }

    private fun initECSRecorder() {
        if (getProperty(PROP_ELASTIC_ENABLED, false)) {
            val host = getProperty<String>(PROP_ELASTIC_HOST)
            val port = getProperty(PROP_ELASTIC_PORT, 80)
            val scheme = getProperty(PROP_ELASTIC_SCHEME, "http")

            log.info("JPut is using Elasticsearch recorder: $host")

            val client = ElasticsearchClientWrapper(RestClient.builder(HttpHost(host, port, scheme)))
            resultRecorders.add(ElasticsearchResultRecorder(client))
        }
    }

    fun destroy() {
        resultRecorders.forEach {
            try {
                it.flush()
            } catch (e: Exception) {
                log.warn("Failed to flush results", e)
            }
            try {
                it.destroy()
            } catch (e: Exception) {
                log.warn("Failed to close the recorder", e)
            }
        }
    }

    fun addPropertySource(source: PropertySource) {
        propertySources.add(source)
    }

    companion object {

        private const val PREFIX = "jput."

        const val PROP_ENABLED = PREFIX + "enabled"

        const val PROP_REPORTER_CLASS = PREFIX + "reporter.class"

        const val PROP_ELASTIC_ENABLED = PREFIX + "storage.elastic.enabled"

        const val PROP_ELASTIC_HOST = PREFIX + "storage.elastic.host"

        const val PROP_ELASTIC_PORT = PREFIX + "storage.elastic.port"

        const val PROP_ELASTIC_SCHEME = PREFIX + "storage.elastic.scheme"

        const val PROP_ELASTIC_INDEX = PREFIX + "storage.elastic.index"

        const val PROP_ELASTIC_ENV_IDENTIFIERS = PREFIX + "storage.elastic.env.identifiers"

        const val PROP_STORAGE_FILE_ENABLED = PREFIX + "storage.file.enabled"

        const val PROP_PATH_TO_STORAGE_FILE = PREFIX + "storage.file.path"

        const val PROP_ENV_PARAMS = PREFIX + "env.custom.params"

    }

}
